package com.example.project_management.feature.dashboard;

import com.example.project_management.feature.dashboard.dto.DashboardOverviewDto;
import com.example.project_management.feature.dashboard.dto.TaskTrendDto;
import com.example.project_management.feature.dashboard.dto.UserPerformanceDto;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardOverviewDto getOverview(Long projectId);
    Map<String, Long> getTaskStatusDistribution(Long projectId);
    List<UserPerformanceDto> getUserPerformance(Long projectId);
}
