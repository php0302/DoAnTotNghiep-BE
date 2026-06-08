package com.example.project_management.feature.task.dto;

import com.example.project_management.feature.task.TaskStatus;

public class TaskStatusUpdateResponse {
    private boolean success;
    private String message;
    private TaskStatusData data;

    public TaskStatusUpdateResponse() {}

    public TaskStatusUpdateResponse(boolean success, String message, Long id, TaskStatus status) {
        this.success = success;
        this.message = message;
        this.data = new TaskStatusData(id, status);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TaskStatusData getData() {
        return data;
    }

    public void setData(TaskStatusData data) {
        this.data = data;
    }

    public static class TaskStatusData {
        private Long id;
        private TaskStatus status;

        public TaskStatusData() {}

        public TaskStatusData(Long id, TaskStatus status) {
            this.id = id;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public void setStatus(TaskStatus status) {
            this.status = status;
        }
    }
}
