package com.example.project_management.feature.task;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.task.dto.TaskPageResponse;
import com.example.project_management.feature.task.dto.TaskRequest;
import com.example.project_management.feature.task.dto.TaskResponse;
import com.example.project_management.feature.task.dto.TaskSearchRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ─── Search & Filter ────────────────────────────────────────────────────────
    /**
     * GET /api/v1/tasks/search
     *
     * Query params (tất cả đều optional):
     *   keyword    - tìm kiếm theo tên task (contains, case-insensitive)
     *   status     - TODO | IN_PROGRESS | DONE
     *   priority   - LOW | MEDIUM | HIGH
     *   assigneeId - ID của user được assign
     *   projectId  - ID của project
     *   startDate  - deadline >= startDate  (format: yyyy-MM-dd)
     *   endDate    - deadline <= endDate    (format: yyyy-MM-dd)
     *   overdue    - true = chỉ lấy task quá hạn
     *   page       - số trang (0-based, mặc định 0)
     *   size       - số item/trang (mặc định 20, tối đa 100)
     *   sortBy     - deadline | priority | createdAt | status | title
     *   sortDir    - ASC | DESC
     *
     * Ví dụ: GET /api/v1/tasks/search?keyword=UI&status=IN_PROGRESS&page=0&size=10&sortBy=deadline&sortDir=ASC
     */
    @GetMapping("/tasks/search")
    public ResponseEntity<ApiResponse<TaskPageResponse>> searchTasks(
            @ModelAttribute TaskSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.searchTasks(request)));
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAuthority('CREATE_TASK') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId, @RequestBody @Valid TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(taskService.createTask(projectId, request)));
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getProjectTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByProjectId(projectId)));
    }

    @GetMapping("/tasks/my")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getMyTasks() {
        return ResponseEntity.ok(ApiResponse.success(taskService.getMyTasks()));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(taskId)));
    }

    @PutMapping("/tasks/{taskId}")
    @PreAuthorize("hasAuthority('EDIT_TASK') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId, @RequestBody @Valid TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTask(taskId, request)));
    }



    @PatchMapping("/tasks/{taskId}/assign")
    @PreAuthorize("hasAuthority('ASSIGN_TASK') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignTask(
            @PathVariable Long taskId, @RequestParam Long userId) {
        taskService.assignTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasAuthority('DELETE_TASK') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
