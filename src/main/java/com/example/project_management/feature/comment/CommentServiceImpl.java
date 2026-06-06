package com.example.project_management.feature.comment;

import com.example.project_management.exception.ForbiddenException;
import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.comment.dto.CommentRequest;
import com.example.project_management.feature.comment.dto.CommentResponse;
import com.example.project_management.feature.notification.NotificationService;
import com.example.project_management.feature.notification.NotificationType;
import com.example.project_management.feature.project.ProjectMemberRepository;
import com.example.project_management.feature.realtime.RealtimeMessage;
import com.example.project_management.feature.realtime.WebSocketBroadcastService;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskRepository;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final WebSocketBroadcastService broadcastService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              CommentMentionRepository commentMentionRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository,
                              ProjectMemberRepository projectMemberRepository,
                              NotificationService notificationService,
                              WebSocketBroadcastService broadcastService) {
        this.commentRepository = commentRepository;
        this.commentMentionRepository = commentMentionRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
        this.broadcastService = broadcastService;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long taskId, CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Kiểm tra author là thành viên project
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(
                task.getProject().getId(), currentUserId);
        if (!isMember && !isAdminOrManager()) {
            throw new ForbiddenException("Bạn không phải thành viên của dự án này");
        }

        String content = request.content() != null ? request.content().trim() : "";
        // Cho phép comment chỉ chứa ảnh (content là zero-width space '\u200b')
        boolean isImageOnlyComment = content.equals("\u200b") || content.isBlank();
        if (isImageOnlyComment) {
            content = ""; // Lưu rỗng vào DB, frontend sẽ hiển thị ảnh
        }
        if (content.length() > 1000) throw new InvalidRequestException("Comment tối đa 1000 ký tự");

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(author);
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);

        // Parse và xử lý @mention
        processMentions(saved, content, task, author);

        // Notify assignee nếu có và khác author
        if (task.getAssignedTo() != null
                && !task.getAssignedTo().getId().equals(author.getId())) {
            String msg = author.getFullName() + " đã bình luận trong task: '" + task.getTitle() + "'";
            notificationService.createAndPush(task.getAssignedTo(), msg,
                    NotificationType.COMMENT_ADDED, task.getId());
        }

        CommentResponse commentResponse = CommentResponse.fromEntity(saved);

        // Broadcast COMMENT_CREATED to all project members viewing this project
        Long projectId = task.getProject().getId();
        Long actorId = SecurityUtil.getCurrentUserId().orElse(null);
        String actorName = author.getFullName();
        broadcastService.broadcastToProject(projectId,
                RealtimeMessage.of("COMMENT_CREATED", projectId, actorId, actorName, commentResponse));

        return commentResponse;
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        boolean isAuthor = comment.getUser().getId().equals(currentUserId);
        if (!isAuthor && !isAdminOrManager()) {
            throw new ForbiddenException("Bạn không có quyền sửa comment này");
        }

        String content = request.content().trim();
        if (content.isEmpty()) throw new InvalidRequestException("Nội dung comment không được rỗng");
        if (content.length() > 1000) throw new InvalidRequestException("Comment tối đa 1000 ký tự");

        comment.setContent(content);

        // Xoá mention cũ và parse lại
        commentMentionRepository.deleteByCommentId(commentId);
        Comment saved = commentRepository.save(comment);
        processMentions(saved, content, comment.getTask(), comment.getUser());

        return CommentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        boolean isAuthor = comment.getUser().getId().equals(currentUserId);
        if (!isAuthor && !isAdminOrManager()) {
            throw new ForbiddenException("Bạn không có quyền xoá comment này");
        }

        commentRepository.delete(comment); // cascade xoá mention tự động
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void processMentions(Comment comment, String content, Task task, User author) {
        Set<String> usernames = MentionParser.extractUsernames(content);
        if (usernames.isEmpty()) return;

        for (String username : usernames) {
            // Bỏ qua mention chính mình
            if (username.equalsIgnoreCase(author.getUsername())) continue;

            userRepository.findByUsername(username).ifPresent(mentionedUser -> {
                // Chỉ mention user là thành viên project
                boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(
                        task.getProject().getId(), mentionedUser.getId());
                if (!isMember) return;

                // Chống duplicate mention trong cùng 1 comment
                boolean alreadyMentioned = commentMentionRepository
                        .existsByCommentIdAndMentionedUserId(comment.getId(), mentionedUser.getId());
                if (alreadyMentioned) return;

                commentMentionRepository.save(new CommentMention(comment, mentionedUser));

                // Gửi notification realtime qua WebSocket
                String msg = author.getFullName() + " đã nhắc đến bạn trong task: '"
                        + task.getTitle() + "'";
                notificationService.createAndPush(mentionedUser, msg,
                        NotificationType.MENTIONED, task.getId());
            });
        }
    }

    private boolean isAdminOrManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_PROJECT_MANAGER"));
    }
}
