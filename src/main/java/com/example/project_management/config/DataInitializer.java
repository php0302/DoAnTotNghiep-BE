package com.example.project_management.config;

import com.example.project_management.feature.role.Permission;
import com.example.project_management.feature.role.RoleEntity;
import com.example.project_management.feature.role.RoleRepository;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Khởi tạo dữ liệu mặc định khi ứng dụng khởi động:
 * - 3 Role hệ thống: ADMIN, PROJECT_MANAGER, MEMBER với permissions tương ứng
 * - Migrate các User cũ (nếu chưa có role) sang MEMBER
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== DataInitializer: Khởi tạo dữ liệu mặc định ===");

        RoleEntity adminRole = ensureRole("ADMIN", "Quản trị viên hệ thống", true, adminPermissions());
        RoleEntity pmRole = ensureRole("PROJECT_MANAGER", "Quản lý dự án", true, pmPermissions());
        RoleEntity memberRole = ensureRole("MEMBER", "Nhân viên", true, memberPermissions());

        // Fix database: khi thêm @JoinColumn role_id, MySQL có thể gán giá trị mặc định
        // là 0
        try {
            jdbcTemplate.update("UPDATE users SET role_id = ? WHERE role_id = 0", memberRole.getId());
        } catch (Exception e) {
            log.warn("Không thể cập nhật role_id = 0: " + e.getMessage());
        }

        // Migrate User chưa có role sang MEMBER
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == null)
                .forEach(u -> {
                    u.setRole(memberRole);
                    userRepository.save(u);
                    log.info("  → Gán role MEMBER cho user: {}", u.getEmail());
                });

        log.info("=== DataInitializer: Hoàn tất ===");
    }

    private RoleEntity ensureRole(String name, String description, boolean systemRole, Set<Permission> permissions) {
        return roleRepository.findByName(name).orElseGet(() -> {
            log.info("  → Tạo role mặc định: {}", name);
            RoleEntity role = new RoleEntity(name, description, systemRole);
            role.setPermissions(permissions);
            return roleRepository.save(role);
        });
    }

    private Set<Permission> adminPermissions() {
        return EnumSet.allOf(Permission.class); // Admin có tất cả quyền
    }

    private Set<Permission> pmPermissions() {
        return EnumSet.of(
                Permission.VIEW_USERS,
                Permission.VIEW_ROLES,
                Permission.VIEW_ALL_PROJECTS,
                Permission.CREATE_PROJECT,
                Permission.EDIT_PROJECT,
                Permission.MANAGE_PROJECT_MEMBERS,
                Permission.VIEW_TASKS,
                Permission.CREATE_TASK,
                Permission.EDIT_TASK,
                Permission.DELETE_TASK,
                Permission.ASSIGN_TASK,
                Permission.CREATE_COMMENT,
                Permission.VIEW_DASHBOARD,
                Permission.VIEW_REPORTS);
    }

    private Set<Permission> memberPermissions() {
        return EnumSet.of(
                Permission.VIEW_TASKS,
                Permission.CREATE_TASK,
                Permission.EDIT_TASK,
                Permission.CREATE_COMMENT);
    }
}
