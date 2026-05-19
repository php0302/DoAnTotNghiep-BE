package com.example.project_management.feature.worklog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WorkLogRequest(
        @NotNull(message = "taskId không được để trống")
        Long taskId,

        @NotNull(message = "Số giờ không được để trống")
        @Min(value = 0, message = "Số giờ phải lớn hơn 0")
        @Max(value = 24, message = "Số giờ không được vượt quá 24")
        Double hoursLogged,

        LocalDate logDate,       // Nếu null → dùng ngày hiện tại

        String description
) {}
