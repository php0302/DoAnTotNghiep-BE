package com.example.project_management.feature.task;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic JPA Specification cho chức năng Search & Filter Task.
 * Mỗi tiêu chí filter chỉ được áp dụng nếu giá trị không null/blank.
 */
public class TaskSpecification {

    private TaskSpecification() {}

    /**
     * Build Specification từ các tham số filter.
     *
     * @param keyword    Từ khóa tìm kiếm trong title (LIKE %keyword%)
     * @param status     Trạng thái task (TODO, IN_PROGRESS, DONE)
     * @param priority   Mức độ ưu tiên (LOW, MEDIUM, HIGH)
     * @param assigneeId ID của user được assign
     * @param projectId  ID của project
     * @param startDate  Ngày bắt đầu khoảng deadline (>= startDate)
     * @param endDate    Ngày kết thúc khoảng deadline (<= endDate)
     * @param overdue    Nếu true: chỉ lấy task quá hạn (deadline < hôm nay AND status != DONE)
     */
    public static Specification<Task> build(
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId,
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean overdue
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 0. Exclude tasks of soft-deleted projects
            predicates.add(cb.equal(root.get("project").get("isDeleted"), false));

            // 1. Keyword search: LIKE %keyword% trên title (case-insensitive)
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase().trim() + "%")
                );
            }

            // 2. Filter theo status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 3. Filter theo priority
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            // 4. Filter theo assignee
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assigneeId));
            }

            // 5. Filter theo project
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            // 6. Deadline range filter
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deadline"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("deadline"), endDate));
            }

            // 7. Overdue filter: deadline < today AND status != DONE
            if (Boolean.TRUE.equals(overdue)) {
                predicates.add(cb.lessThan(root.get("deadline"), LocalDate.now()));
                predicates.add(cb.notEqual(root.get("status"), TaskStatus.DONE));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
