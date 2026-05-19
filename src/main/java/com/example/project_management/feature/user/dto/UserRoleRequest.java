package com.example.project_management.feature.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(
        @NotNull(message = "Role ID không được để trống")
        Long roleId
) {}
