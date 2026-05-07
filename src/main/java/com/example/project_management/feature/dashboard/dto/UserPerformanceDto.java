package com.example.project_management.feature.dashboard.dto;

public record UserPerformanceDto(
        Long userId,
        String username,
        String fullName,
        long completedTasks,
        long totalAssigned
) {
    public double getCompletionRate() {
        if (totalAssigned == 0) return 0.0;
        return Math.round(((double) completedTasks / totalAssigned) * 100.0 * 100.0) / 100.0;
    }
}
