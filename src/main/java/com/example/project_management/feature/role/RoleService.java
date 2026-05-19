package com.example.project_management.feature.role;

import com.example.project_management.feature.role.dto.RoleRequest;
import com.example.project_management.feature.role.dto.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
    RoleResponse getRoleById(Long id);
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRole(Long id, RoleRequest request);
    void deleteRole(Long id);
    List<Permission> getAllPermissions();
}
