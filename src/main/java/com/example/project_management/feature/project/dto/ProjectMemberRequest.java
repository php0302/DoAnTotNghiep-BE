package com.example.project_management.feature.project.dto;

import com.example.project_management.feature.project.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record ProjectMemberRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Role is required")
        ProjectRole role
) {}

