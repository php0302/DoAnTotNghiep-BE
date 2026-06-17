package com.example.project_management.feature.task;

import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.project.Project;
import com.example.project_management.feature.project.ProjectMemberRepository;
import com.example.project_management.feature.project.ProjectRepository;
import com.example.project_management.feature.realtime.RealtimeMessage;
import com.example.project_management.feature.realtime.WebSocketBroadcastService;
import com.example.project_management.feature.task.dto.TaskPageResponse;
import com.example.project_management.feature.task.dto.TaskRequest;
import com.example.project_management.feature.task.dto.TaskResponse;
import com.example.project_management.feature.task.dto.TaskSearchRequest;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.feature.notification.NotificationType;
import com.example.project_management.feature.notification.NotificationService;
import com.example.project_management.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WebSocketBroadcastService broadcastService;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository, UserRepository userRepository,
                           NotificationService notificationService,
                           WebSocketBroadcastService broadcastService,
                           TaskStatusHistoryRepository taskStatusHistoryRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.broadcastService = broadcastService;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
    }

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        if (request.deadline() != null && request.deadline().isBefore(java.time.LocalDate.now())) {
            throw new InvalidRequestException("Deadline không được chọn trong quá khứ");
        }

        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (request.deadline() != null && project.getEndDate() != null && request.deadline().isAfter(project.getEndDate())) {
            throw new InvalidRequestException("Deadline không được vượt quá ngày kết thúc dự án (" + project.getEndDate() + ")");
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDeadline(request.deadline());
        task.setProject(project);
        if (request.estimatedHours() != null) task.setEstimatedHours(request.estimatedHours());

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())) {
                throw new InvalidRequestException("Assignee must be a member of the project");
            }
            task.setAssignedTo(assignee);
            Task savedTask = taskRepository.save(task);
            String msg = "Bạn vừa được giao task mới: '" + savedTask.getTitle() + "' trong dự án '" + project.getName() + "'";
            notificationService.createAndPush(assignee, msg, NotificationType.TASK_ASSIGNED, savedTask.getId());
            TaskResponse taskResponse = TaskResponse.fromEntity(savedTask);
            // Broadcast TASK_CREATED to all project members
            Long actorId = SecurityUtil.getCurrentUserId().orElse(null);
            String actorName = SecurityUtil.getCurrentUserEmail().orElse("unknown");
            broadcastService.broadcastToProject(projectId,
                    RealtimeMessage.of("TASK_CREATED", projectId, actorId, actorName, taskResponse));
            return taskResponse;
        }

        Task saved = taskRepository.save(task);
        TaskResponse taskResponse = TaskResponse.fromEntity(saved);
        // Broadcast TASK_CREATED to all project members
        Long actorId = SecurityUtil.getCurrentUserId().orElse(null);
        String actorName = SecurityUtil.getCurrentUserEmail().orElse("unknown");
        broadcastService.broadcastToProject(projectId,
                RealtimeMessage.of("TASK_CREATED", projectId, actorId, actorName, taskResponse));
        return taskResponse;
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        if (request.deadline() != null && request.deadline().isBefore(java.time.LocalDate.now())) {
            throw new InvalidRequestException("Deadline không được chọn trong quá khứ");
        }

        Task task = taskRepository.findByIdAndProjectIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Project project = task.getProject();
        if (request.deadline() != null && project.getEndDate() != null && request.deadline().isAfter(project.getEndDate())) {
            throw new InvalidRequestException("Deadline không được vượt quá ngày kết thúc dự án (" + project.getEndDate() + ")");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        task.setDeadline(request.deadline());
        if (request.estimatedHours() != null) task.setEstimatedHours(request.estimatedHours());

        Long actorId = SecurityUtil.getCurrentUserId().orElse(null);
        String actorName = SecurityUtil.getCurrentUserEmail().orElse("unknown");
        Long projectId = task.getProject().getId();

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())) {
                throw new InvalidRequestException("Assignee must be a member of the project");
            }
            // Thông báo nếu người được giao có thay đổi
            boolean assigneeChanged = task.getAssignedTo() == null
                    || !task.getAssignedTo().getId().equals(assignee.getId());
            task.setAssignedTo(assignee);
            Task saved = taskRepository.save(task);
            if (assigneeChanged) {
                String msg = "Bạn vừa được giao task: '" + saved.getTitle() + "' trong dự án '" + saved.getProject().getName() + "'";
                notificationService.createAndPush(assignee, msg, NotificationType.TASK_ASSIGNED, saved.getId());
            } else if (request.deadline() != null && !request.deadline().equals(task.getDeadline())) {
                String msg = "Deadline task '" + saved.getTitle() + "' đã được cập nhật thành " + request.deadline();
                notificationService.createAndPush(assignee, msg, NotificationType.DEADLINE_UPDATED, saved.getId());
            }
            TaskResponse taskResponse = TaskResponse.fromEntity(saved);
            // Broadcast TASK_UPDATED
            broadcastService.broadcastToProject(projectId,
                    RealtimeMessage.of("TASK_UPDATED", projectId, actorId, actorName, taskResponse));
            return taskResponse;
        } else {
            task.setAssignedTo(null);
        }

        Task saved = taskRepository.save(task);
        TaskResponse taskResponse = TaskResponse.fromEntity(saved);
        // Broadcast TASK_UPDATED
        broadcastService.broadcastToProject(projectId,
                RealtimeMessage.of("TASK_UPDATED", projectId, actorId, actorName, taskResponse));
        return taskResponse;
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findByIdAndProjectIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Kiểm tra quyền: chỉ người được giao task hoặc ADMIN/PM mới được đổi trạng thái
        boolean isPrivileged = isAdminOrManager();
        if (!isPrivileged) {
            Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            Long assignedToId  = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
            if (!java.util.Objects.equals(currentUserId, assignedToId)) {
                throw new com.example.project_management.exception.ForbiddenException(
                        "Chỉ người được giao task mới có thể cập nhật trạng thái");
            }
        }

        // Validate workflow transition
        TaskStatus oldStatus = task.getStatus();
        if (!TaskStatus.isValidTransition(oldStatus, status)) {
            throw new InvalidRequestException(
                    "Chuyển trạng thái từ " + oldStatus.getDisplayName() + " sang " + status.getDisplayName() + " không hợp lệ");
        }

        // Kiểm tra chuyển đổi trạng thái đặc thù theo yêu cầu phân quyền:
        // - Từ IN_REVIEW sang TESTING: chỉ PM/Admin mới được phép kéo
        // - Từ TESTING sang DONE: chỉ PM/Admin mới được phép kéo
        if ((oldStatus == TaskStatus.IN_REVIEW && status == TaskStatus.TESTING) ||
            (oldStatus == TaskStatus.TESTING && status == TaskStatus.DONE)) {
            if (!isPrivileged) {
                throw new com.example.project_management.exception.ForbiddenException(
                        "Chỉ Quản lý dự án (PM) hoặc Quản trị viên mới được phép chuyển sang trạng thái " + status.getDisplayName());
            }
        }

        task.setStatus(status);
        Task saved = taskRepository.save(task);

        // Ghi log lịch sử đổi trạng thái
        Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        User changedBy = null;
        if (currentUserId != null) {
            changedBy = userRepository.findById(currentUserId).orElse(null);
        }
        TaskStatusHistory history = new TaskStatusHistory(saved, oldStatus, status, changedBy);
        taskStatusHistoryRepository.save(history);

        // Broadcast TASK_STATUS_CHANGED to all project members
        Long actorId = SecurityUtil.getCurrentUserId().orElse(null);
        String actorName = SecurityUtil.getCurrentUserEmail().orElse("unknown");
        Long projectId = saved.getProject().getId();
        broadcastService.broadcastToProject(projectId,
                RealtimeMessage.of("TASK_STATUS_CHANGED", projectId, actorId, actorName, TaskResponse.fromEntity(saved)));
    }

    @Override
    @Transactional
    public void updateTaskPriority(Long taskId, TaskPriority priority) {
        Task task = taskRepository.findByIdAndProjectIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        task.setPriority(priority);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndProjectIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), userId)) {
            throw new InvalidRequestException("User is not a member of the project");
        }
        task.setAssignedTo(assignee);
        taskRepository.save(task);

        String notificationMsg = "Bạn vừa được giao task: '" + task.getTitle() + "' trong dự án '" + task.getProject().getName() + "'";
        notificationService.createAndPush(assignee, notificationMsg, NotificationType.TASK_ASSIGNED, task.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findByIdAndProjectIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return TaskResponse.fromEntity(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectIdAndProjectIsDeletedFalse(projectId).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        return taskRepository.findByAssignedToIdAndProjectIsDeletedFalse(currentUserId).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPageResponse searchTasks(TaskSearchRequest req) {
        // ── Security: lấy danh sách projectId mà user hiện tại là thành viên ──
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));

        boolean isPrivileged = isAdminOrManager();

        // Lấy projectId được phép xem
        // - ADMIN/PM: xem tất cả (không lọc theo project membership)
        // - MEMBER: chỉ xem project họ tham gia
        Set<Long> allowedProjectIds = null;
        if (!isPrivileged) {
            allowedProjectIds = projectMemberRepository
                    .findByUserId(currentUserId)
                    .stream()
                    .map(pm -> pm.getProject().getId())
                    .collect(Collectors.toSet());

            // Nếu filter theo projectId cụ thể, kiểm tra quyền
            if (req.getProjectId() != null && !allowedProjectIds.contains(req.getProjectId())) {
                throw new com.example.project_management.exception.ForbiddenException(
                        "Bạn không có quyền xem task của project này");
            }
        }

        // ── Build Specification ──────────────────────────────────────────────
        Specification<Task> spec = TaskSpecification.build(
                req.getKeyword(),
                req.getStatus(),
                req.getPriority(),
                req.getAssigneeId(),
                req.getProjectId(),
                req.getStartDate(),
                req.getEndDate(),
                req.getOverdue()
        );

        // Nếu là MEMBER, thêm điều kiện giới hạn theo allowedProjectIds
        if (!isPrivileged && allowedProjectIds != null) {
            final Set<Long> finalAllowed = allowedProjectIds;
            Specification<Task> memberScope = (root, query, cb) ->
                    root.get("project").get("id").in(finalAllowed);
            spec = spec.and(memberScope);
        }

        // ── Build Pageable ───────────────────────────────────────────────────
        // Chỉ cho phép sort theo các trường hợp lệ để chống injection
        List<String> validSortFields = Arrays.asList("deadline", "priority", "createdAt", "status", "title");
        String sortBy = validSortFields.contains(req.getSortBy()) ? req.getSortBy() : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(req.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), Sort.by(direction, sortBy));

        // ── Execute Query ────────────────────────────────────────────────────
        Page<Task> page = taskRepository.findAll(spec, pageable);

        List<TaskResponse> content = page.getContent().stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());

        return new TaskPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findByIdAndProjectIsDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        Long projectId = task.getProject().getId();
        taskRepository.delete(task);

        // Broadcast TASK_DELETED to all project members
        Long actorId = SecurityUtil.getCurrentUserId().orElse(null);
        String actorName = SecurityUtil.getCurrentUserEmail().orElse("unknown");
        broadcastService.broadcastToProject(projectId,
                RealtimeMessage.of("TASK_DELETED", projectId, actorId, actorName,
                        java.util.Map.of("taskId", taskId)));
    }

    /** Kiểm tra user hiện tại có role cao không */
    private boolean isAdminOrManager() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_PROJECT_MANAGER"));
    }
}
