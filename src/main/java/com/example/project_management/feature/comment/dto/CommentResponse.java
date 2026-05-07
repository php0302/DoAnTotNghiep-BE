package com.example.project_management.feature.comment.dto;

import com.example.project_management.feature.comment.Comment;
import com.example.project_management.feature.comment.CommentMention;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        Long taskId,
        AuthorDto author,
        List<MentionDto> mentions,
        Instant createdAt,
        Instant updatedAt
) {
    public record AuthorDto(Long id, String username, String fullName) {}
    public record MentionDto(Long id, String username, String fullName) {}

    public static CommentResponse fromEntity(Comment comment) {
        List<MentionDto> mentionDtos = comment.getMentions().stream()
                .map(m -> new MentionDto(
                        m.getMentionedUser().getId(),
                        m.getMentionedUser().getUsername(),
                        m.getMentionedUser().getFullName()
                ))
                .toList();

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                new AuthorDto(
                        comment.getUser().getId(),
                        comment.getUser().getUsername(),
                        comment.getUser().getFullName()
                ),
                mentionDtos,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
