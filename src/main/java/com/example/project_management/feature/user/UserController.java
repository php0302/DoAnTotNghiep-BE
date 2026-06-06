package com.example.project_management.feature.user;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.user.dto.AdminResetPasswordRequest;
import com.example.project_management.feature.user.dto.ChangePasswordRequest;
import com.example.project_management.feature.user.dto.CreateUserRequest;
import com.example.project_management.feature.user.dto.UserResponse;
import com.example.project_management.feature.user.dto.UserRoleRequest;
import com.example.project_management.feature.user.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    /** Admin tạo tài khoản nhân viên — phải chọn role ngay */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(userService.createUser(request)));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserRole(id, request.roleId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateMyProfile(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserProfile(id, request)));
    }

    /** Nhân viên đổi mật khẩu (kể cả lần đầu bắt buộc) */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Admin đặt lại mật khẩu cho người dùng khác không cần mật khẩu cũ */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(
            @PathVariable Long id,
            @RequestBody @Valid AdminResetPasswordRequest request) {
        userService.resetUserPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
