package com.example.project_management.feature.worklog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long>, JpaSpecificationExecutor<WorkLog> {

    /** Lấy tất cả work log của một task (để hiển thị trong TaskDetail) */
    List<WorkLog> findByTaskIdOrderByLogDateDesc(Long taskId);

    /**
     * Lấy work log của một user trong khoảng ngày (để trang Báo cáo hằng ngày)
     * Kết quả sắp xếp theo logDate DESC
     */
    @Query("""
        SELECT wl FROM WorkLog wl
        WHERE wl.user.id = :userId
          AND wl.logDate >= :startDate
          AND wl.logDate <= :endDate
        ORDER BY wl.logDate DESC, wl.createdAt DESC
    """)
    List<WorkLog> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Tính tổng số giờ log của một task
     */
    @Query("SELECT COALESCE(SUM(wl.hoursLogged), 0) FROM WorkLog wl WHERE wl.task.id = :taskId")
    Double sumHoursByTaskId(@Param("taskId") Long taskId);

    /**
     * Lấy tất cả work log trong khoảng ngày (cho Admin xem toàn bộ)
     */
    @Query("""
        SELECT wl FROM WorkLog wl
        WHERE wl.logDate >= :startDate
          AND wl.logDate <= :endDate
        ORDER BY wl.logDate DESC, wl.user.id, wl.createdAt DESC
    """)
    List<WorkLog> findAllByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
