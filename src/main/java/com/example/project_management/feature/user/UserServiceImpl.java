package com.example.project_management.feature.user;

import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.role.RoleEntity;
import com.example.project_management.feature.role.RoleRepository;
import com.example.project_management.feature.user.dto.UpdateProfileRequest;
import com.example.project_management.feature.user.dto.UserResponse;
import com.example.project_management.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", "current user context"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Chức vụ", "id", roleId));
        user.setRole(role);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        String email = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", "current user context"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return internalUpdateProfile(user, request);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return internalUpdateProfile(user, request);
    }

    private UserResponse internalUpdateProfile(User user, UpdateProfileRequest request) {
        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new com.example.project_management.exception.ConflictException("Email đã được sử dụng bởi tài khoản khác!");
            }
            user.setEmail(request.email());
        }
        user.setFullName(request.fullName());
        return UserResponse.fromEntity(userRepository.save(user));
    }
}
