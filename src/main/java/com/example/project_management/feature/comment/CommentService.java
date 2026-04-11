package com.example.project_management.feature.comment;

import com.example.project_management.feature.comment.dto.CommentRequest;
import com.example.project_management.feature.comment.dto.CommentResponse;
import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long taskId, CommentRequest request);
    List<CommentResponse> getCommentsByTaskId(Long taskId);
    void deleteComment(Long commentId);
}
