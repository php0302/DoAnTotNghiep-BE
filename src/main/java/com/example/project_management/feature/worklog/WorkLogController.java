package com.example.project_management.feature.worklog;

import com.example.project_management.feature.worklog.dto.DailyReportResponse;
import com.example.project_management.feature.worklog.dto.WorkLogRequest;
import com.example.project_management.feature.worklog.dto.WorkLogResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/worklogs")
public class WorkLogController {

    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    /**
     * POST /api/worklogs
     * Tạo một work log entry mới
     */
    @PostMapping
    public ResponseEntity<WorkLogResponse> createWorkLog(@Valid @RequestBody WorkLogRequest request) {
        return ResponseEntity.ok(workLogService.createWorkLog(request));
    }

    /**
     * GET /api/worklogs/task/{taskId}
     * Lấy danh sách work log của một task (hiển thị trong TaskDetail)
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<WorkLogResponse>> getByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(workLogService.getWorkLogsByTask(taskId));
    }

    /**
     * GET /api/worklogs/report?startDate=2024-01-01&endDate=2024-01-07&userId=5
     * Lấy báo cáo hằng ngày. userId là tuỳ chọn (Admin có thể xem của người khác).
     */
    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(workLogService.getDailyReport(userId, projectId, startDate, endDate));
    }

    /**
     * DELETE /api/worklogs/{id}
     * Xoá một work log (chủ sở hữu hoặc Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkLog(@PathVariable Long id) {
        workLogService.deleteWorkLog(id);
        return ResponseEntity.noContent().build();
    }
}
