package com.example.project_management.feature.worklog.dto;

import com.example.project_management.feature.worklog.WorkLog;

import java.time.Instant;
import java.time.LocalDate;

public record WorkLogResponse(
        Long id,
        Long taskId,
        String taskTitle,
        Long projectId,
        String projectName,
        Long userId,
        String userFullName,
        LocalDate logDate,
        Double hoursLogged,
        String description,
        Instant createdAt
) {
    public static WorkLogResponse fromEntity(WorkLog wl) {
        return new WorkLogResponse(
                wl.getId(),
                wl.getTask().getId(),
                wl.getTask().getTitle(),
                wl.getTask().getProject().getId(),
                wl.getTask().getProject().getName(),
                wl.getUser().getId(),
                wl.getUser().getFullName(),
                wl.getLogDate(),
                wl.getHoursLogged(),
                wl.getDescription(),
                wl.getCreatedAt()
        );
    }
}
