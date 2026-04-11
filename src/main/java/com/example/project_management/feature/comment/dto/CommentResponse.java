package com.example.project_management.feature.comment.dto;

import com.example.project_management.feature.comment.Comment;
import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Long taskId,
        Long userId,
        String username,
        Instant createdAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getCreatedAt()
        );
    }
}
