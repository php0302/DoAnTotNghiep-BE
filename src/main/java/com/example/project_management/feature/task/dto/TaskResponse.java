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
        Double estimatedHours,
        Long projectId,
        String projectName,
        Long assignedToId,
        String assignedToName,
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
                task.getEstimatedHours(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getFullName() : null,
                task.getCreatedAt()
        );
    }
}

