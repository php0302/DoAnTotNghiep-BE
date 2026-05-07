package com.example.project_management.feature.dashboard;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.dashboard.dto.DashboardOverviewDto;
import com.example.project_management.feature.dashboard.dto.UserPerformanceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getOverview(
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOverview(projectId)));
    }

    @GetMapping("/tasks/status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTaskStatusDistribution(
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTaskStatusDistribution(projectId)));
    }

    @GetMapping("/users/performance")
    public ResponseEntity<ApiResponse<List<UserPerformanceDto>>> getUserPerformance(
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUserPerformance(projectId)));
    }
}
