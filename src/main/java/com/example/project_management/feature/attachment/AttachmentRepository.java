package com.example.project_management.feature.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /** Lấy tất cả file đính kèm của một task, sắp xếp mới nhất lên đầu */
    List<Attachment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    /** Lấy tất cả file đính kèm của một comment, sắp xếp mới nhất lên đầu */
    List<Attachment> findByCommentIdOrderByCreatedAtDesc(Long commentId);
}
