package com.example.project_management.feature.attachment;

import com.example.project_management.feature.comment.Comment;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachments_task",    columnList = "task_id"),
        @Index(name = "idx_attachments_comment", columnList = "comment_id"),
        @Index(name = "idx_attachments_user",    columnList = "uploaded_by")
})
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên file lưu trên disk: uuid + extension */
    @Column(nullable = false, length = 255)
    private String fileName;

    /** Tên gốc của file do user upload — hiển thị trên UI */
    @Column(nullable = false, length = 255)
    private String originalName;

    /** URL/đường dẫn để truy cập file */
    @Column(nullable = false, length = 1000)
    private String fileUrl;

    /** MIME type: image/png, application/pdf, ... */
    @Column(nullable = false, length = 100)
    private String fileType;

    /** Kích thước file tính bằng bytes */
    @Column(nullable = false)
    private Long fileSize;

    /** Người upload */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /** File đính kèm vào task (nullable — file có thể thuộc comment) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    /** File đính kèm vào comment (nullable — file có thể thuộc task) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public Attachment() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
}
