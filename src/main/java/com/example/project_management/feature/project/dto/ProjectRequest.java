package com.example.project_management.feature.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank(message = "Project name is required")
        String name,

        String description,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate
) {
    /**
     * Custom validation: endDate phải sau startDate nếu được cung cấp.
     * Gọi thủ công trong Service để kiểm tra.
     */
    public boolean isEndDateValid() {
        if (endDate == null) return true;
        return !endDate.isBefore(startDate);
    }
}
