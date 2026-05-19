package com.example.project_management.feature.user;

import com.example.project_management.feature.user.dto.UserResponse;
import com.example.project_management.feature.user.dto.UpdateProfileRequest;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUserRole(Long userId, Long roleId);
    UserResponse updateMyProfile(UpdateProfileRequest request);
    UserResponse updateUserProfile(Long id, UpdateProfileRequest request);
}
