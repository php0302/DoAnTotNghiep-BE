package com.example.project_management;

import com.example.project_management.feature.role.RoleEntity;
import com.example.project_management.feature.role.RoleRepository;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class ProjectManagementApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void contextLoads() {
        if (!userRepository.existsByUsername("admin")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
            RoleEntity adminRole = roleRepository.findByName("ADMIN").orElse(null);
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setFullName("System Admin");
            admin.setPassword(encoder.encode("123456"));
            admin.setActive(true);
            admin.setMustChangePassword(false);
            if (adminRole != null) {
                admin.setRole(adminRole);
            }
            userRepository.save(admin);
            System.out.println("=== ADMIN USER CREATED SUCCESS ===");
        } else {
            System.out.println("=== ADMIN USER ALREADY EXISTS ===");
        }
    }

}
