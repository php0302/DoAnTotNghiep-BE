package com.example.project_management.feature.project.dto;

import com.example.project_management.feature.project.Project;
import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Long createdById
) {
    public static ProjectResponse fromEntity(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy() != null ? project.getCreatedBy().getId() : null
        );
    }
}
