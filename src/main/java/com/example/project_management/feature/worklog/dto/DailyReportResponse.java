package com.example.project_management.feature.worklog.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Response tổng hợp cho trang Báo cáo hằng ngày:
 * - Danh sách work log trong khoảng ngày
 * - Tổng giờ đã làm
 */
public record DailyReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        Double totalHours,
        List<WorkLogResponse> logs
) {}
