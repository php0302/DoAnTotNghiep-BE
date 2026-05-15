package com.example.project_management.feature.task.dto;

import com.example.project_management.feature.task.TaskPriority;
import com.example.project_management.feature.task.TaskStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO chứa tất cả tham số cho chức năng Search & Filter Task.
 * Nhận qua @RequestParam hoặc @ModelAttribute từ Controller.
 */
public class TaskSearchRequest {

    /** Từ khóa tìm kiếm theo title */
    private String keyword;

    /** Lọc theo trạng thái */
    private TaskStatus status;

    /** Lọc theo độ ưu tiên */
    private TaskPriority priority;

    /** Lọc theo ID người được assign */
    private Long assigneeId;

    /** Lọc theo ID project */
    private Long projectId;

    /** Deadline >= startDate */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** Deadline <= endDate */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** Nếu true: chỉ lấy task quá hạn */
    private Boolean overdue;

    // ─── Pagination & Sort ─────────────────────────────────────────────

    private int page = 0;
    private int size = 20;

    /**
     * Trường sort. Hợp lệ: deadline, priority, createdAt, status
     * Mặc định: createdAt
     */
    private String sortBy = "createdAt";

    /**
     * Hướng sort: ASC hoặc DESC
     */
    private String sortDir = "DESC";

    // ─── Getters & Setters ──────────────────────────────────────────────

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getOverdue() { return overdue; }
    public void setOverdue(Boolean overdue) { this.overdue = overdue; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(size, 100); } // giới hạn max 100

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
}
