package com.example.project_management.feature.role.dto;

import com.example.project_management.feature.role.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleRequest(
        @NotBlank(message = "Tên chức vụ không được để trống")
        @Size(max = 50, message = "Tên chức vụ không quá 50 ký tự")
        String name,

        @Size(max = 255, message = "Mô tả không quá 255 ký tự")
        String description,

        Set<Permission> permissions
) {}
