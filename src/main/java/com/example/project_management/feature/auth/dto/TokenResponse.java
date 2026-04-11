package com.example.project_management.feature.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}
