package com.example.project_management.feature.task;

import com.example.project_management.feature.project.ProjectMember;
import com.example.project_management.feature.project.ProjectMemberRepository;
import com.example.project_management.feature.project.ProjectRole;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import com.example.project_management.feature.notification.NotificationType;
import com.example.project_management.feature.notification.NotificationService;
import com.example.project_management.feature.notification.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TaskDeadlineScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskDeadlineScheduler.class);

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;

    public TaskDeadlineScheduler(TaskRepository taskRepository,
                                 NotificationRepository notificationRepository,
                                 UserRepository userRepository,
                                 ProjectMemberRepository projectMemberRepository,
                                 NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
    }

    /**
     * Quét các task quá hạn mỗi phút (hoặc theo cấu hình)
     */
    @Scheduled(cron = "${app.scheduler.deadline-check:0 * * * * ?}")
    @Transactional
    public void checkDeadlines() {
        log.info("Starting task deadline check scheduler...");
        LocalDate today = LocalDate.now();
        List<Task> overdueTasks = taskRepository.findByStatusNotAndDeadlineBeforeAndProjectIsDeletedFalse(TaskStatus.DONE, today);

        if (overdueTasks.isEmpty()) {
            log.info("No overdue tasks found.");
            return;
        }

        log.info("Found {} overdue tasks. Processing notifications...", overdueTasks.size());

        for (Task task : overdueTasks) {
            // Check if deadline notification has already been sent to avoid spamming
            boolean alreadyNotified = notificationRepository.existsByTaskIdAndType(task.getId(), NotificationType.DEADLINE_PASSED);
            if (alreadyNotified) {
                continue;
            }

            log.info("Task '{}' (ID: {}) has passed its deadline ({}). Generating notifications...",
                    task.getTitle(), task.getId(), task.getDeadline());

            // Build unique set of recipients:
            Set<User> recipients = new HashSet<>();

            // 1. Assignee (Người phụ trách)
            if (task.getAssignedTo() != null) {
                recipients.add(task.getAssignedTo());
            }

            // 2. Project Managers of the project
            Long projectId = task.getProject().getId();
            List<ProjectMember> pms = projectMemberRepository.findByProjectIdAndRole(projectId, ProjectRole.PROJECT_MANAGER);
            for (ProjectMember pm : pms) {
                if (pm.getUser() != null) {
                    recipients.add(pm.getUser());
                }
            }

            // 3. Project Creator (often the PM / Owner)
            User creator = task.getProject().getCreatedBy();
            if (creator != null) {
                recipients.add(creator);
            }

            // 4. System Administrators
            List<User> systemAdmins = userRepository.findByRole_Name("ADMIN");
            recipients.addAll(systemAdmins);

            // Construct notification message
            String msg = "Task '" + task.getTitle() + "' trong dự án '" + task.getProject().getName() 
                    + "' đã quá hạn deadline (" + task.getDeadline() + ") mà chưa hoàn thành.";

            // Send notification to each unique recipient
            for (User user : recipients) {
                if (user.isActive()) { // Only notify active users
                    try {
                        notificationService.createAndPush(user, msg, NotificationType.DEADLINE_PASSED, task.getId());
                    } catch (Exception e) {
                        log.error("Failed to push deadline notification to user {}: {}", user.getEmail(), e.getMessage());
                    }
                }
            }
        }
        log.info("Finished task deadline check scheduler.");
    }
}
