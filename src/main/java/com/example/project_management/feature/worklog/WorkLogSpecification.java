package com.example.project_management.feature.worklog;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkLogSpecification {

    public static Specification<WorkLog> build(
            Long userId,
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            Set<Long> allowedProjectIds,
            boolean isPrivileged) {
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("logDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("logDate"), endDate));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("task").get("project").get("id"), projectId));
            }

            // Nếu không phải admin/PM toàn quyền, chỉ được xem log của chính mình 
            // HOẶC log của các member khác trong những dự án mà mình quản lý (nếu có).
            // Ở đây `allowedProjectIds` chứa các projectId mà user hiện tại đang quản lý.
            if (!isPrivileged) {
                Predicate isOwnLog = cb.equal(root.get("user").get("id"), userId != null ? userId : -1L); // Nếu ko có allowedProjectIds thì chỉ xem của mình
                
                if (allowedProjectIds != null && !allowedProjectIds.isEmpty()) {
                    Predicate isInAllowedProjects = root.get("task").get("project").get("id").in(allowedProjectIds);
                    predicates.add(cb.or(isOwnLog, isInAllowedProjects));
                } else {
                    predicates.add(isOwnLog);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
