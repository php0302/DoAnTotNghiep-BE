package com.example.project_management.feature.task;

import com.example.project_management.feature.task.dto.TaskStatusUpdateRequest;
import com.example.project_management.feature.task.dto.TaskStatusUpdateResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskWorkflowController {

    private final TaskService taskService;

    public TaskWorkflowController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PatchMapping({"/api/tasks/{id}/status", "/api/v1/tasks/{id}/status"})
    public ResponseEntity<TaskStatusUpdateResponse> updateTaskStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        
        taskService.updateTaskStatus(id, request.getStatus());
        
        return ResponseEntity.ok(new TaskStatusUpdateResponse(
                true,
                "Task status updated",
                id,
                request.getStatus()
        ));
    }
}
