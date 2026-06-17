package com.example.project_management.feature.project.dto;

import com.example.project_management.feature.project.Project;
import com.example.project_management.feature.user.dto.UserResponse;
import com.example.project_management.feature.task.Task;
import com.example.project_management.feature.task.TaskStatus;
import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Long createdById,
        List<UserResponse> members,
        Integer progress
) {
    public static ProjectResponse fromEntity(Project project) {
        List<UserResponse> membersList = java.util.Collections.emptyList();
        if (project.getMembers() != null) {
            membersList = project.getMembers().stream()
                    .map(pm -> UserResponse.fromEntity(pm.getUser()))
                    .collect(java.util.stream.Collectors.toList());
        }

        int progressPercent = 0;
        List<Task> tasksList = project.getTasks();
        if (tasksList != null && !tasksList.isEmpty()) {
            long doneCount = tasksList.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .count();
            progressPercent = (int) Math.round((double) doneCount / tasksList.size() * 100);
        }

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy() != null ? project.getCreatedBy().getId() : null,
                membersList,
                progressPercent
        );
    }
}
