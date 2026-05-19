package com.example.project_management.feature.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    /** Kiểm tra xem có User nào đang sử dụng Role này không */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role.id = :roleId")
    boolean existsUserByRoleId(@Param("roleId") Long roleId);
}
