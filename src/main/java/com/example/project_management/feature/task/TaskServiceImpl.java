package com.example.project_management.feature.task;

import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.project.Project;
import com.example.project_management.feature.project.ProjectMemberRepository;
import com.example.project_management.feature.project.ProjectRepository;
import com.example.project_management.feature.task.dto.TaskRequest;
import com.example.project_management.feature.task.dto.TaskResponse;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.feature.notification.NotificationService;
import com.example.project_management.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository, UserRepository userRepository,
                           NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDeadline(request.deadline());
        task.setProject(project);

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())) {
                throw new InvalidRequestException("Assignee must be a member of the project");
            }
            task.setAssignedTo(assignee);
        }

        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        task.setDeadline(request.deadline());

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), assignee.getId())) {
                throw new InvalidRequestException("Assignee must be a member of the project");
            }
            task.setAssignedTo(assignee);
        } else {
            task.setAssignedTo(null);
        }

        return TaskResponse.fromEntity(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        task.setStatus(status);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void updateTaskPriority(Long taskId, TaskPriority priority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        task.setPriority(priority);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), userId)) {
            throw new InvalidRequestException("User is not a member of the project");
        }
        task.setAssignedTo(assignee);
        taskRepository.save(task);
        
        String notificationMsg = "You have been assigned to task: '" + task.getTitle() + "'";
        notificationService.createNotification(assignee, notificationMsg);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return TaskResponse.fromEntity(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        return taskRepository.findByAssignedToId(currentUserId).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }
        taskRepository.deleteById(taskId);
    }
}
