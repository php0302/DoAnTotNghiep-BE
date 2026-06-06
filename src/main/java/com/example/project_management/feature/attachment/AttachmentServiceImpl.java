package com.example.project_management.feature.attachment;

import com.example.project_management.exception.ForbiddenException;
import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.attachment.dto.AttachmentResponse;
import com.example.project_management.feature.comment.Comment;
import com.example.project_management.feature.comment.CommentRepository;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskRepository;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    /** Danh sách MIME type được phép upload */
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",       // .xlsx
            "application/zip"
    );

    @Value("${file.max-size:10485760}") // Mặc định 10MB
    private long maxFileSize;

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository       taskRepository;
    private final CommentRepository    commentRepository;
    private final UserRepository       userRepository;
    private final StorageService       storageService;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 TaskRepository taskRepository,
                                 CommentRepository commentRepository,
                                 UserRepository userRepository,
                                 StorageService storageService) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository       = taskRepository;
        this.commentRepository    = commentRepository;
        this.userRepository       = userRepository;
        this.storageService       = storageService;
    }

    // ── Upload ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AttachmentResponse uploadToTask(Long taskId, MultipartFile file) {
        validateFile(file);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User uploader = getCurrentUser();

        try {
            String url = storageService.store(file, "tasks/" + taskId);
            Attachment attachment = buildAttachment(file, url, uploader);
            attachment.setTask(task);
            return toResponse(attachmentRepository.save(attachment));
        } catch (IOException e) {
            log.error("Upload failed for task {}: {}", taskId, e.getMessage());
            throw new RuntimeException("Không thể lưu file. Vui lòng thử lại.");
        }
    }

    @Override
    @Transactional
    public AttachmentResponse uploadToComment(Long commentId, MultipartFile file) {
        validateFile(file);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User uploader = getCurrentUser();

        try {
            String url = storageService.store(file, "comments/" + commentId);
            Attachment attachment = buildAttachment(file, url, uploader);
            attachment.setComment(comment);
            return toResponse(attachmentRepository.save(attachment));
        } catch (IOException e) {
            log.error("Upload failed for comment {}: {}", commentId, e.getMessage());
            throw new RuntimeException("Không thể lưu file. Vui lòng thử lại.");
        }
    }

    // ── Query ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }
        return attachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getByComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        return attachmentRepository.findByCommentIdOrderByCreatedAtDesc(commentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        User currentUser = getCurrentUser();
        boolean isOwner = attachment.getUploadedBy().getId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole().getName());

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Bạn không có quyền xóa file này");
        }

        // Xóa file vật lý — nếu lỗi vẫn xóa metadata
        try {
            storageService.delete(attachment.getFileUrl());
        } catch (IOException e) {
            log.warn("Could not delete physical file {}: {}", attachment.getFileUrl(), e.getMessage());
        }

        attachmentRepository.delete(attachment);
        log.info("Attachment {} deleted by user {}", attachmentId, currentUser.getId());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /** Validate file trước khi upload */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File không được để trống");
        }
        if (file.getSize() > maxFileSize) {
            long limitMb = maxFileSize / (1024 * 1024);
            throw new InvalidRequestException(
                    "File vượt quá giới hạn " + limitMb + "MB. File của bạn: "
                    + String.format("%.1f", file.getSize() / 1024.0 / 1024.0) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidRequestException(
                    "Định dạng file không được hỗ trợ: " + contentType
                    + ". Chỉ chấp nhận: Ảnh, PDF, DOCX, XLSX, ZIP");
        }
    }

    /** Tạo entity Attachment từ file + uploader */
    private Attachment buildAttachment(MultipartFile file, String url, User uploader) {
        Attachment a = new Attachment();
        // Lấy tên file từ URL (phần sau dấu / cuối cùng)
        a.setFileName(url.substring(url.lastIndexOf('/') + 1));
        a.setOriginalName(file.getOriginalFilename());
        a.setFileUrl(url);
        a.setFileType(file.getContentType());
        a.setFileSize(file.getSize());
        a.setUploadedBy(uploader);
        return a;
    }

    /** Convert entity → DTO */
    private AttachmentResponse toResponse(Attachment a) {
        AttachmentResponse r = new AttachmentResponse();
        r.setId(a.getId());
        r.setOriginalName(a.getOriginalName());
        r.setFileUrl(a.getFileUrl());
        r.setFileType(a.getFileType());
        r.setFileSize(a.getFileSize());
        r.setUploadedById(a.getUploadedBy().getId());
        r.setUploadedByName(a.getUploadedBy().getFullName());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }

    /** Lấy user đang đăng nhập từ SecurityContext */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
