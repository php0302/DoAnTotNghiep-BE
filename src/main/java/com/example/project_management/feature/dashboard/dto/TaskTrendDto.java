package com.example.project_management.feature.dashboard.dto;

import java.time.LocalDate;

public record TaskTrendDto(
        LocalDate date,
        long completedCount
) {}
