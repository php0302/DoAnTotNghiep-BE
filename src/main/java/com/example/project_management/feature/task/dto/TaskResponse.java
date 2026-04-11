package com.example.project_management.feature.task.dto;

import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskPriority;
import com.example.project_management.feature.task.TaskStatus;
import java.time.LocalDate;
import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate deadline,
        Long projectId,
        Long assignedToId,
        Instant createdAt
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDeadline(),
                task.getProject().getId(),
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getCreatedAt()
        );
    }
}
