package com.example.project_management.feature.task;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.task.dto.TaskRequest;
import com.example.project_management.feature.task.dto.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}/tasks")
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
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId, @RequestBody @Valid TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTask(taskId, request)));
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<Void>> updateTaskStatus(
            @PathVariable Long taskId, @RequestParam TaskStatus status) {
        taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/tasks/{taskId}/assign")
    public ResponseEntity<ApiResponse<Void>> assignTask(
            @PathVariable Long taskId, @RequestParam Long userId) {
        taskService.assignTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
