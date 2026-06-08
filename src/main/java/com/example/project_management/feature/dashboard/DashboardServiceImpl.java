package com.example.project_management.feature.dashboard;

import com.example.project_management.exception.ForbiddenException;
import com.example.project_management.feature.dashboard.dto.DashboardOverviewDto;
import com.example.project_management.feature.dashboard.dto.UserPerformanceDto;
import com.example.project_management.feature.project.ProjectMemberRepository;
import com.example.project_management.feature.project.ProjectRepository;
import com.example.project_management.feature.task.TaskRepository;
import com.example.project_management.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public DashboardServiceImpl(TaskRepository taskRepository,
                                ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private void checkAccess(Long projectId) {
        if (projectId != null && !isAdminOrManager()) {
            Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
                throw new ForbiddenException("Bạn không có quyền xem báo cáo của dự án này");
            }
        }
    }

    private boolean isAdminOrManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_PROJECT_MANAGER"));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewDto getOverview(Long projectId) {
        checkAccess(projectId);
        LocalDate today = LocalDate.now();

        long totalProjects, totalTasks, overdueTasks, dueSoonTasks;

        if (projectId != null) {
            totalProjects = 1;
            totalTasks    = taskRepository.countTotalTasksForProject(projectId);
            overdueTasks  = taskRepository.countOverdueTasksForProject(projectId, today);
            dueSoonTasks  = taskRepository.countDueSoonTasksForProject(projectId, today, today.plusDays(7));
        } else {
            totalProjects = projectRepository.count();
            totalTasks    = taskRepository.countTotalTasksAll();
            overdueTasks  = taskRepository.countOverdueTasksAll(today);
            dueSoonTasks  = taskRepository.countDueSoonTasksAll(today, today.plusDays(7));
        }

        return new DashboardOverviewDto(totalProjects, totalProjects, 0L, totalTasks, overdueTasks, dueSoonTasks);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTaskStatusDistribution(Long projectId) {
        checkAccess(projectId);

        Map<String, Long> distribution = new HashMap<>();
        distribution.put("TODO", 0L);
        distribution.put("IN_PROGRESS", 0L);
        distribution.put("IN_REVIEW", 0L);
        distribution.put("TESTING", 0L);
        distribution.put("DONE", 0L);
        distribution.put("BLOCKED", 0L);

        List<Object[]> rows = projectId != null
                ? taskRepository.countTasksByStatusForProject(projectId)
                : taskRepository.countTasksByStatusAll();

        for (Object[] row : rows) {
            String status = (String) row[0];
            long count = toLong(row[1]);
            distribution.put(status, count);
        }
        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPerformanceDto> getUserPerformance(Long projectId) {
        checkAccess(projectId);

        // Build map: userId -> totalAssigned
        Map<Long, Long> totalMap = new HashMap<>();
        List<Object[]> assignedRows = projectId != null
                ? taskRepository.countAssignedByUserForProject(projectId)
                : taskRepository.countAssignedByUserAll();
        for (Object[] row : assignedRows) {
            totalMap.put(toLong(row[0]), toLong(row[1]));
        }

        List<Object[]> completedRows = projectId != null
                ? taskRepository.countCompletedByUserForProject(projectId)
                : taskRepository.countCompletedByUserAll();

        List<UserPerformanceDto> dtos = new ArrayList<>();
        for (Object[] row : completedRows) {
            Long userId     = toLong(row[0]);
            String username = (String) row[1];
            String fullName = row[2] != null ? (String) row[2] : username;
            long completed  = toLong(row[3]);
            long total      = totalMap.getOrDefault(userId, completed);
            dtos.add(new UserPerformanceDto(userId, username, fullName, completed, total));
        }
        return dtos;
    }

    /** Safely convert BigInteger / Long / Integer từ native query sang long */
    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long l) return l;
        if (obj instanceof BigInteger bi) return bi.longValue();
        if (obj instanceof Integer i) return i.longValue();
        if (obj instanceof Number n) return n.longValue();
        return 0L;
    }
}
