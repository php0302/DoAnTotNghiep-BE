package com.example.project_management.feature.task.dto;

import java.util.List;

/**
 * Response DTO chuẩn cho kết quả phân trang task search.
 */
public record TaskPageResponse(
        List<TaskResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
