package com.example.project_management.feature.auth;

import com.example.project_management.feature.auth.dto.LoginRequest;
import com.example.project_management.feature.auth.dto.RegisterRequest;
import com.example.project_management.feature.auth.dto.RefreshTokenRequest;
import com.example.project_management.feature.auth.dto.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    void register(RegisterRequest request);
    TokenResponse refreshToken(RefreshTokenRequest request);
}

