package com.example.project_management.feature.user;

import com.example.project_management.feature.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUserRole(Long userId, Long roleId);
}
