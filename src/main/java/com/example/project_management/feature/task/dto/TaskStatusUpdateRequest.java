package com.example.project_management.feature.task.dto;

import com.example.project_management.feature.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class TaskStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private TaskStatus status;

    public TaskStatusUpdateRequest() {}

    public TaskStatusUpdateRequest(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
