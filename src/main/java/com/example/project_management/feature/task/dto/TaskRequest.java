package com.example.project_management.feature.task.dto;

import com.example.project_management.feature.task.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        String title,
        
        String description,
        
        TaskPriority priority,
        
        LocalDate deadline,
        
        Long assignedToId,

        Double estimatedHours
) {}
