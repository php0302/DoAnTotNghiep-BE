package com.example.project_management.feature.worklog;

import com.example.project_management.feature.worklog.dto.DailyReportResponse;
import com.example.project_management.feature.worklog.dto.WorkLogRequest;
import com.example.project_management.feature.worklog.dto.WorkLogResponse;

import java.time.LocalDate;
import java.util.List;

public interface WorkLogService {

    /** Tạo một work log entry mới cho user hiện tại */
    WorkLogResponse createWorkLog(WorkLogRequest request);

    /** Lấy danh sách work log của một task */
    List<WorkLogResponse> getWorkLogsByTask(Long taskId);

    /**
     * Lấy báo cáo ngày của user hiện tại (hoặc user chỉ định nếu là Admin/Manager).
     * @param targetUserId  null → lấy của user đang đăng nhập; non-null → chỉ Admin/Manager được dùng
     */
    DailyReportResponse getDailyReport(Long targetUserId, Long projectId, LocalDate startDate, LocalDate endDate);

    /** Xoá work log (chỉ được xoá của mình hoặc Admin) */
    void deleteWorkLog(Long workLogId);
}
