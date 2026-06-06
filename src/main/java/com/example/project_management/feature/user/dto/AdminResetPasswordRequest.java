package com.example.project_management.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body khi Admin đặt lại mật khẩu cho tài khoản khác.
 * Không yêu cầu mật khẩu cũ — admin có toàn quyền.
 */
public record AdminResetPasswordRequest(

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
        String newPassword,

        /**
         * Nếu true → bật lại cờ mustChangePassword,
         * buộc người dùng phải đổi mật khẩu khi đăng nhập lại.
         */
        boolean forceChangeOnLogin
) {}
