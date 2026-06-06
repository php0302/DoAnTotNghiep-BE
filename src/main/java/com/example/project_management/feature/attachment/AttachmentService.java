package com.example.project_management.feature.attachment;

import com.example.project_management.feature.attachment.dto.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    /** Upload file và đính kèm vào task */
    AttachmentResponse uploadToTask(Long taskId, MultipartFile file);

    /** Upload file và đính kèm vào comment */
    AttachmentResponse uploadToComment(Long commentId, MultipartFile file);

    /** Lấy danh sách file đính kèm của task */
    List<AttachmentResponse> getByTask(Long taskId);

    /** Lấy danh sách file đính kèm của comment */
    List<AttachmentResponse> getByComment(Long commentId);

    /** Xóa attachment (chỉ người upload hoặc ADMIN mới được xóa) */
    void delete(Long attachmentId);
}
