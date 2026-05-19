package com.example.project_management.feature.role.dto;

import com.example.project_management.feature.role.Permission;
import com.example.project_management.feature.role.RoleEntity;

import java.time.Instant;
import java.util.Set;

public record RoleResponse(
        Long id,
        String name,
        String description,
        Set<Permission> permissions,
        boolean systemRole,
        long userCount,
        Instant createdAt
) {
    public static RoleResponse fromEntity(RoleEntity role, long userCount) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions(),
                role.isSystemRole(),
                userCount,
                role.getCreatedAt()
        );
    }
}
