package com.example.project_management.feature.comment;

import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.comment.dto.CommentRequest;
import com.example.project_management.feature.comment.dto.CommentResponse;
import com.example.project_management.feature.notification.NotificationService;
import com.example.project_management.feature.notification.NotificationType;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskRepository;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentServiceImpl(CommentRepository commentRepository, TaskRepository taskRepository,
                              UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long taskId, CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(currentUser);
        comment.setContent(request.content());
        Comment savedComment = commentRepository.save(comment);

        // Notify assigned user if the commenter is not the assignee
        if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(currentUser.getId())) {
            String commenterName = currentUser.getUsername();
            String notificationMsg = commenterName + " đã bình luận trong task: '" + task.getTitle() + "'";
            notificationService.createAndPush(
                    task.getAssignedTo(),
                    notificationMsg,
                    NotificationType.COMMENT_ADDED,
                    task.getId()
            );
        }

        return CommentResponse.fromEntity(savedComment);
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
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        commentRepository.deleteById(commentId);
    }
}
