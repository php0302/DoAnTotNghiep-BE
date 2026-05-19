package com.example.project_management.feature.user.dto;

import com.example.project_management.feature.role.Permission;
import com.example.project_management.feature.user.User;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Long roleId,
        String role,        // Tên role (VD: "ADMIN", "PROJECT_MANAGER")
        Set<Permission> permissions,
        boolean isActive,
        boolean mustChangePassword,
        Instant createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getId() : null,
                user.getRole() != null ? user.getRole().getName() : null,
                user.getRole() != null ? user.getRole().getPermissions() : Set.of(),
                user.isActive(),
                user.isMustChangePassword(),
                user.getCreatedAt()
        );
    }
}
