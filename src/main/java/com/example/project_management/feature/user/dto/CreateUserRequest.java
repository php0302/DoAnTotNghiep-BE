package com.example.project_management.feature.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO để Admin tạo tài khoản nhân viên.
 * Role là bắt buộc - Admin phải chọn role ngay khi tạo.
 */
public record CreateUserRequest(

        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
        String username,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải ít nhất 6 ký tự")
        String password,

        @NotNull(message = "Vui lòng chọn chức vụ cho nhân viên")
        Long roleId
) {}
