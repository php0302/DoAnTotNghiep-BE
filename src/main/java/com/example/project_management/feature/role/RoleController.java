package com.example.project_management.feature.role;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.role.dto.RoleRequest;
import com.example.project_management.feature.role.dto.RoleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /** Lấy danh sách tất cả chức vụ — Admin và PM đều được xem */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(roleService.getAllRoles()));
    }

    /** Lấy thông tin chi tiết một chức vụ */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getRoleById(id)));
    }

    /** Tạo chức vụ mới — chỉ Admin */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@RequestBody @Valid RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roleService.createRole(request)));
    }

    /** Cập nhật chức vụ — chỉ Admin */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.updateRole(id, request)));
    }

    /**
     * Xóa chức vụ — chỉ Admin.
     * Sẽ trả về lỗi 400 kèm thông báo tiếng Việt nếu còn User đang dùng.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Lấy danh sách tất cả Permission có trong hệ thống */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(roleService.getAllPermissions()));
    }
}
