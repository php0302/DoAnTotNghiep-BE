package com.example.project_management.feature.user;

import com.example.project_management.feature.user.dto.AdminResetPasswordRequest;
import com.example.project_management.feature.user.dto.ChangePasswordRequest;
import com.example.project_management.feature.user.dto.CreateUserRequest;
import com.example.project_management.feature.user.dto.UpdateProfileRequest;
import com.example.project_management.feature.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUserRole(Long userId, Long roleId);
    UserResponse updateMyProfile(UpdateProfileRequest request);
    UserResponse updateUserProfile(Long id, UpdateProfileRequest request);

    /** Admin tạo tài khoản nhân viên, buộc đổi mật khẩu lần đầu */
    UserResponse createUser(CreateUserRequest request);

    /** Nhân viên đổi mật khẩu (kể cả lần đầu bắt buộc) */
    void changePassword(ChangePasswordRequest request);

    /** Admin đặt lại mật khẩu cho người dùng khác không cần mật khẩu cũ */
    void resetUserPassword(Long userId, AdminResetPasswordRequest request);

    /** Admin xóa (vô hiệu hóa) tài khoản nhân viên */
    void deleteUser(Long id);
}
