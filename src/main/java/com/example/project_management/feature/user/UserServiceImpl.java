package com.example.project_management.feature.user;

import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.role.RoleEntity;
import com.example.project_management.feature.role.RoleRepository;
import com.example.project_management.feature.user.dto.ChangePasswordRequest;
import com.example.project_management.feature.user.dto.CreateUserRequest;
import com.example.project_management.feature.user.dto.UpdateProfileRequest;
import com.example.project_management.feature.user.dto.UserResponse;
import com.example.project_management.feature.realtime.RealtimeMessage;
import com.example.project_management.feature.realtime.WebSocketBroadcastService;
import com.example.project_management.security.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebSocketBroadcastService broadcastService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           WebSocketBroadcastService broadcastService) {
        this.userRepository   = userRepository;
        this.roleRepository   = roleRepository;
        this.passwordEncoder  = passwordEncoder;
        this.broadcastService = broadcastService;
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

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Kiểm tra trùng username / email
        if (userRepository.existsByUsername(request.username())) {
            throw new com.example.project_management.exception.ConflictException("Tên đăng nhập đã được sử dụng!");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new com.example.project_management.exception.ConflictException("Email đã được sử dụng!");
        }

        // Lấy role (bắt buộc)
        RoleEntity role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Chức vụ", "id", request.roleId()));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setMustChangePassword(true); // Bắt buộc đổi mật khẩu lần đầu
        user.setActive(true);

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", "current user context"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Verify mật khẩu hiện tại
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidRequestException("Mật khẩu hiện tại không đúng!");
        }

        // Cập nhật mật khẩu mới và tắt flag buộc đổi
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Broadcast realtime cho Admin đang xem UserManagement
        broadcastService.broadcastToAdmins(
            RealtimeMessage.of(
                "PASSWORD_CHANGED",
                null,
                user.getId(),
                user.getFullName(),
                java.util.Map.of("userId", user.getId(), "mustChangePassword", false)
            )
        );
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

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Không cho phép tự xóa tài khoản của chính mình (nếu cần có thể kiểm tra)
        // User currentUser = ... (từ SecurityUtil)
        
        user.setActive(false);
        userRepository.save(user);

        // Broadcast realtime cho Admin đang xem UserManagement
        broadcastService.broadcastToAdmins(
            RealtimeMessage.of(
                "USER_DEACTIVATED",
                null,
                user.getId(),
                user.getFullName(),
                java.util.Map.of("userId", user.getId(), "isActive", false)
            )
        );
    }
}
