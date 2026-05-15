package com.example.project_management.feature.project;

import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.project.dto.ProjectMemberRequest;
import com.example.project_management.feature.project.dto.ProjectRequest;
import com.example.project_management.feature.project.dto.ProjectResponse;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskRepository;
import com.example.project_management.feature.comment.CommentRepository;
import com.example.project_management.feature.notification.NotificationService;
import com.example.project_management.feature.notification.NotificationType;
import com.example.project_management.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectMemberRepository projectMemberRepository,
                              UserRepository userRepository,
                              TaskRepository taskRepository,
                              CommentRepository commentRepository,
                              NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if (!request.isEndDateValid()) {
            throw new com.example.project_management.exception.InvalidRequestException(
                    "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user id"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepository.save(project);

        // Add creator as PROJECT_MANAGER automatically
        ProjectMember member = new ProjectMember();
        member.setProject(savedProject);
        member.setUser(currentUser);
        member.setRole(com.example.project_management.feature.user.Role.PROJECT_MANAGER);
        projectMemberRepository.save(member);

        return ProjectResponse.fromEntity(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        if (!request.isEndDateValid()) {
            throw new com.example.project_management.exception.InvalidRequestException(
                    "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());

        return ProjectResponse.fromEntity(projectRepository.save(project));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Kiểm tra quyền: Admin/PM thấy tất cả; member khác chỉ thấy project mình tham gia
        boolean isPrivileged = isAdminOrManager();
        if (!isPrivileged) {
            Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(id, currentUserId);
            if (!isMember) {
                throw new com.example.project_management.exception.ForbiddenException(
                        "Bạn không phải thành viên của dự án này");
            }
        }
        return ProjectResponse.fromEntity(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        boolean isPrivileged = isAdminOrManager();
        if (isPrivileged) {
            // Admin/PM thấy tất cả
            return projectRepository.findAll().stream()
                    .map(ProjectResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        // Member thường chỉ thấy project mình tham gia
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        return projectRepository.findByMemberUserId(currentUserId).stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** Kiểm tra user hiện tại có role cao không (ADMIN hoặc PROJECT_MANAGER) */
    private boolean isAdminOrManager() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_PROJECT_MANAGER"));
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Lấy danh sách thành viên để thông báo
        List<ProjectMember> members = projectMemberRepository.findByProjectId(id);
        
        // 1. Delete all comments inside tasks of this project
        // 2. Delete the tasks
        List<Task> tasks = taskRepository.findByProjectId(id);
        for (Task task : tasks) {
            commentRepository.deleteByTaskId(task.getId());
            taskRepository.delete(task);
        }

        // 3. Delete all project members
        projectMemberRepository.deleteByProjectId(id);

        // 4. Finally delete the project
        projectRepository.deleteById(id);

        // 5. Gửi thông báo xoá dự án đến các thành viên (ngoại trừ người đang thực hiện xoá)
        Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        for (ProjectMember pm : members) {
            if (!pm.getUser().getId().equals(currentUserId)) {
                String msg = "Dự án '" + project.getName() + "' đã bị xoá bởi Quản trị viên.";
                notificationService.createAndPush(pm.getUser(), msg, NotificationType.PROJECT_DELETED, id);
            }
        }
    }

    @Override
    @Transactional
    public void addMemberToProject(Long projectId, ProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new com.example.project_management.exception.ConflictException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.role());
        projectMemberRepository.save(member);

        // Notify user about being added to the project
        String msg = "Bạn đã được thêm vào dự án: " + project.getName();
        notificationService.createAndPush(user, msg, NotificationType.PROJECT_ASSIGNED, projectId);
    }

    @Override
    @Transactional
    public void removeMemberFromProject(Long projectId, Long userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("ProjectMember", "userId", userId);
        }
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.example.project_management.feature.user.dto.UserResponse> getProjectMembers(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(pm -> com.example.project_management.feature.user.dto.UserResponse.fromEntity(pm.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.example.project_management.feature.user.dto.UserResponse> suggestMembers(Long projectId, String query) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        String q = (query == null ? "" : query.toLowerCase().trim());
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(ProjectMember::getUser)
                .filter(u -> q.isEmpty()
                        || u.getUsername().toLowerCase().contains(q)
                        || u.getFullName().toLowerCase().contains(q))
                .limit(10)
                .map(com.example.project_management.feature.user.dto.UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
