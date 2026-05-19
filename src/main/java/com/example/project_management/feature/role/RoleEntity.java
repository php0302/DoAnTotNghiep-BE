package com.example.project_management.feature.role;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    /**
     * Danh sách các quyền hạn được gán cho chức vụ này.
     * Lưu trữ ở bảng phụ role_permissions.
     */
    @ElementCollection(targetClass = Permission.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Set<Permission> permissions = new HashSet<>();

    /** Role hệ thống không thể bị xóa (ADMIN, PROJECT_MANAGER, MEMBER) */
    @Column(nullable = false)
    private boolean systemRole = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public RoleEntity() {}

    public RoleEntity(String name, String description, boolean systemRole) {
        this.name = name;
        this.description = description;
        this.systemRole = systemRole;
    }

    // ========== Getters & Setters ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }

    public boolean isSystemRole() { return systemRole; }
    public void setSystemRole(boolean systemRole) { this.systemRole = systemRole; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
