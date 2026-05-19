package com.example.project_management.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO để nhân viên đổi mật khẩu (bao gồm lần đầu đăng nhập).
 */
public record ChangePasswordRequest(

        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
        String currentPassword,

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(min = 6, max = 100, message = "Mật khẩu mới phải ít nhất 6 ký tự")
        String newPassword
) {}
