package com.example.project_management.feature.task;

import com.example.project_management.feature.task.dto.TaskRequest;
import com.example.project_management.feature.task.dto.TaskResponse;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(Long projectId, TaskRequest request);
    TaskResponse updateTask(Long taskId, TaskRequest request);
    void updateTaskStatus(Long taskId, TaskStatus status);
    void updateTaskPriority(Long taskId, TaskPriority priority);
    void assignTask(Long taskId, Long userId);
    TaskResponse getTaskById(Long taskId);
    List<TaskResponse> getTasksByProjectId(Long projectId);
    List<TaskResponse> getMyTasks();
    void deleteTask(Long taskId);
}
