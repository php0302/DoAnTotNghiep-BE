package com.example.project_management.feature.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndIsDeletedFalse(Long id);

    List<Project> findByIsDeletedFalse();

    /** Lấy tất cả project mà user là thành viên */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.user.id = :userId AND p.isDeleted = false")
    List<Project> findByMemberUserId(@Param("userId") Long userId);
}
