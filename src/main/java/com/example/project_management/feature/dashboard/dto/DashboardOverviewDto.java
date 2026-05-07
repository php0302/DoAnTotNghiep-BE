package com.example.project_management.feature.dashboard.dto;

public record DashboardOverviewDto(
        long totalProjects,
        long activeProjects,
        long completedProjects,
        long totalTasks,
        long overdueTasks,
        long dueSoonTasks
) {}
