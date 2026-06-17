package com.example.project_management.feature.project;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByUserId(Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    void deleteByProjectId(Long projectId);
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);
}
