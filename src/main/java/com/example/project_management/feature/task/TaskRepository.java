package com.example.project_management.feature.task;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByProjectIdAndProjectIsDeletedFalse(Long projectId);
    List<Task> findByAssignedToIdAndProjectIsDeletedFalse(Long userId);
    List<Task> findByStatusNotAndDeadlineBeforeAndProjectIsDeletedFalse(TaskStatus status, LocalDate deadline);
    Optional<Task> findByIdAndProjectIsDeletedFalse(Long id);

    // === Dashboard Queries (Native SQL - MySQL) ===

    // -- By Project --
    @Query(value = "SELECT status, COUNT(*) AS cnt FROM tasks WHERE project_id = :pid GROUP BY status", nativeQuery = true)
    List<Object[]> countTasksByStatusForProject(@Param("pid") Long projectId);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE project_id = :pid", nativeQuery = true)
    long countTotalTasksForProject(@Param("pid") Long projectId);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE project_id = :pid AND status <> 'DONE' AND deadline < :today", nativeQuery = true)
    long countOverdueTasksForProject(@Param("pid") Long projectId, @Param("today") LocalDate today);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE project_id = :pid AND status <> 'DONE' AND deadline BETWEEN :today AND :soon", nativeQuery = true)
    long countDueSoonTasksForProject(@Param("pid") Long projectId, @Param("today") LocalDate today, @Param("soon") LocalDate soon);

    @Query(value =
        "SELECT u.id, u.username, u.full_name, COUNT(t.id) AS completed " +
        "FROM tasks t JOIN users u ON t.assigned_to = u.id " +
        "WHERE t.project_id = :pid AND t.status = 'DONE' " +
        "GROUP BY u.id, u.username, u.full_name ORDER BY completed DESC LIMIT 10",
        nativeQuery = true)
    List<Object[]> countCompletedByUserForProject(@Param("pid") Long projectId);

    @Query(value = "SELECT assigned_to, COUNT(*) FROM tasks WHERE project_id = :pid AND assigned_to IS NOT NULL GROUP BY assigned_to", nativeQuery = true)
    List<Object[]> countAssignedByUserForProject(@Param("pid") Long projectId);

    // -- All Projects --
    @Query(value = "SELECT t.status, COUNT(t.id) AS cnt FROM tasks t JOIN projects p ON t.project_id = p.id WHERE p.is_deleted = false GROUP BY t.status", nativeQuery = true)
    List<Object[]> countTasksByStatusAll();

    @Query(value = "SELECT COUNT(t.id) FROM tasks t JOIN projects p ON t.project_id = p.id WHERE p.is_deleted = false", nativeQuery = true)
    long countTotalTasksAll();

    @Query(value = "SELECT COUNT(t.id) FROM tasks t JOIN projects p ON t.project_id = p.id WHERE t.status <> 'DONE' AND t.deadline < :today AND p.is_deleted = false", nativeQuery = true)
    long countOverdueTasksAll(@Param("today") LocalDate today);

    @Query(value = "SELECT COUNT(t.id) FROM tasks t JOIN projects p ON t.project_id = p.id WHERE t.status <> 'DONE' AND t.deadline BETWEEN :today AND :soon AND p.is_deleted = false", nativeQuery = true)
    long countDueSoonTasksAll(@Param("today") LocalDate today, @Param("soon") LocalDate soon);

    @Query(value =
        "SELECT u.id, u.username, u.full_name, COUNT(t.id) AS completed " +
        "FROM tasks t JOIN users u ON t.assigned_to = u.id " +
        "JOIN projects p ON t.project_id = p.id " +
        "WHERE t.status = 'DONE' AND p.is_deleted = false " +
        "GROUP BY u.id, u.username, u.full_name ORDER BY completed DESC LIMIT 10",
        nativeQuery = true)
    List<Object[]> countCompletedByUserAll();

    @Query(value = "SELECT t.assigned_to, COUNT(t.id) FROM tasks t JOIN projects p ON t.project_id = p.id WHERE t.assigned_to IS NOT NULL AND p.is_deleted = false GROUP BY t.assigned_to", nativeQuery = true)
    List<Object[]> countAssignedByUserAll();
}
