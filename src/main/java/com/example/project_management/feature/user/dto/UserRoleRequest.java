package com.example.project_management.feature.user.dto;

import com.example.project_management.feature.user.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {}
