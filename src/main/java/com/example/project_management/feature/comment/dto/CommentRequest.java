package com.example.project_management.feature.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank(message = "Content cannot be blank")
        String content
) {}
