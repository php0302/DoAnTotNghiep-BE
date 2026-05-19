package com.example.project_management.feature.worklog;

import com.example.project_management.exception.ForbiddenException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskRepository;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.feature.worklog.dto.DailyReportResponse;
import com.example.project_management.feature.worklog.dto.WorkLogRequest;
import com.example.project_management.feature.worklog.dto.WorkLogResponse;
import com.example.project_management.feature.project.ProjectMemberRepository;
import com.example.project_management.security.SecurityUtil;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WorkLogServiceImpl implements WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public WorkLogServiceImpl(WorkLogRepository workLogRepository,
                               TaskRepository taskRepository,
                               UserRepository userRepository,
                               ProjectMemberRepository projectMemberRepository) {
        this.workLogRepository = workLogRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    @Transactional
    public WorkLogResponse createWorkLog(WorkLogRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.taskId()));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        WorkLog log = new WorkLog();
        log.setTask(task);
        log.setUser(user);
        log.setHoursLogged(request.hoursLogged());
        log.setLogDate(request.logDate() != null ? request.logDate() : LocalDate.now());
        log.setDescription(request.description());

        return WorkLogResponse.fromEntity(workLogRepository.save(log));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkLogResponse> getWorkLogsByTask(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return workLogRepository.findByTaskIdOrderByLogDateDesc(taskId).stream()
                .map(WorkLogResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyReportResponse getDailyReport(Long targetUserId, Long projectId, LocalDate startDate, LocalDate endDate) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));

        boolean isPrivileged = isAdminOrManager();
        
        java.util.Set<Long> allowedProjectIds = null;
        if (!isPrivileged) {
            allowedProjectIds = projectMemberRepository.findByUserId(currentUserId).stream()
                    .map(pm -> pm.getProject().getId())
                    .collect(java.util.stream.Collectors.toSet());
            
            if (projectId != null && !allowedProjectIds.contains(projectId)) {
                throw new ForbiddenException("Bạn không có quyền xem báo cáo của dự án này");
            }
            if (targetUserId != null && !targetUserId.equals(currentUserId)) {
                // Member chỉ được xem log của chính mình
                throw new ForbiddenException("Bạn không có quyền xem báo cáo của người khác");
            }
        }

        LocalDate from = startDate != null ? startDate : LocalDate.now();
        LocalDate to   = endDate   != null ? endDate   : LocalDate.now();

        Specification<WorkLog> spec = WorkLogSpecification.build(
                targetUserId, projectId, from, to, allowedProjectIds, isPrivileged
        );

        List<WorkLog> logs = workLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "logDate", "createdAt"));
        double total = logs.stream().mapToDouble(WorkLog::getHoursLogged).sum();

        List<WorkLogResponse> responses = logs.stream().map(WorkLogResponse::fromEntity).toList();
        return new DailyReportResponse(from, to, total, responses);
    }

    @Override
    @Transactional
    public void deleteWorkLog(Long workLogId) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));

        WorkLog log = workLogRepository.findById(workLogId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkLog", "id", workLogId));

        boolean isOwner = log.getUser().getId().equals(currentUserId);
        
        if (!isOwner) {
            boolean isPrivileged = isAdminOrManager();
            if (!isPrivileged) {
                // Không phải owner, cũng không phải admin/pm
                throw new ForbiddenException("Bạn không có quyền xoá work log này");
            }
            
            // Nếu là PM, kiểm tra xem có quyền trong dự án này không (nếu là Admin thì luôn được)
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
            if (!isAdmin) {
                boolean isProjectMember = projectMemberRepository.existsByProjectIdAndUserId(
                        log.getTask().getProject().getId(), currentUserId);
                if (!isProjectMember) {
                    throw new ForbiddenException("Bạn không có quyền xoá work log của dự án này");
                }
            }
        }

        workLogRepository.delete(log);
    }

    private boolean isAdminOrManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_PROJECT_MANAGER"));
    }
}
