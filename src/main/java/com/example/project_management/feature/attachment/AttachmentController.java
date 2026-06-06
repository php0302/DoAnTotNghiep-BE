package com.example.project_management.feature.attachment;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.attachment.dto.AttachmentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * POST /api/v1/tasks/{taskId}/attachments
     * Upload file vào task
     */
    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadToTask(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) {
        AttachmentResponse response = attachmentService.uploadToTask(taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    /**
     * POST /api/v1/comments/{commentId}/attachments
     * Upload file vào comment
     */
    @PostMapping("/comments/{commentId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadToComment(
            @PathVariable Long commentId,
            @RequestParam("file") MultipartFile file) {
        AttachmentResponse response = attachmentService.uploadToComment(commentId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    /**
     * GET /api/v1/tasks/{taskId}/attachments
     * Lấy danh sách file đính kèm của task
     */
    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getTaskAttachments(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.getByTask(taskId)));
    }

    /**
     * GET /api/v1/comments/{commentId}/attachments
     * Lấy danh sách file đính kèm của comment
     */
    @GetMapping("/comments/{commentId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getCommentAttachments(
            @PathVariable Long commentId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.getByComment(commentId)));
    }

    /**
     * DELETE /api/v1/attachments/{attachmentId}
     * Xóa attachment (chỉ người upload hoặc ADMIN)
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
