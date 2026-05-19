package com.example.project_management.feature.role;

import com.example.project_management.exception.ConflictException;
import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.role.dto.RoleRequest;
import com.example.project_management.feature.role.dto.RoleResponse;
import com.example.project_management.feature.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll(Sort.by("name")).stream()
                .map(role -> {
                    long count = userRepository.countByRoleId(role.getId());
                    return RoleResponse.fromEntity(role, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        RoleEntity role = findOrThrow(id);
        long count = userRepository.countByRoleId(id);
        return RoleResponse.fromEntity(role, count);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new ConflictException("Chức vụ với tên '" + request.name() + "' đã tồn tại");
        }
        RoleEntity role = new RoleEntity(request.name(), request.description(), false);
        if (request.permissions() != null) {
            role.setPermissions(new HashSet<>(request.permissions()));
        }
        RoleEntity saved = roleRepository.save(role);
        return RoleResponse.fromEntity(saved, 0L);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        RoleEntity role = findOrThrow(id);

        // Kiểm tra trùng tên với role khác
        roleRepository.findByName(request.name()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Chức vụ với tên '" + request.name() + "' đã tồn tại");
            }
        });

        role.setName(request.name());
        role.setDescription(request.description());
        if (request.permissions() != null) {
            role.setPermissions(new HashSet<>(request.permissions()));
        }

        long count = userRepository.countByRoleId(id);
        return RoleResponse.fromEntity(roleRepository.save(role), count);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        RoleEntity role = findOrThrow(id);

        // Không cho xóa role hệ thống
        if (role.isSystemRole()) {
            throw new InvalidRequestException(
                    "Không thể xóa chức vụ hệ thống '" + role.getName() + "'");
        }

        // Chặn xóa nếu còn User đang dùng
        long userCount = userRepository.countByRoleId(id);
        if (userCount > 0) {
            throw new InvalidRequestException(
                    "Không thể xóa chức vụ '" + role.getName() + "' vì đang có "
                    + userCount + " thành viên sử dụng. "
                    + "Vui lòng chuyển họ sang chức vụ khác trước khi xóa.");
        }

        roleRepository.delete(role);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Arrays.asList(Permission.values());
    }

    private RoleEntity findOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chức vụ", "id", id));
    }
}
