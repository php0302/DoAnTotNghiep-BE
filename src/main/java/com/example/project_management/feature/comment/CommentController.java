package com.example.project_management.feature.comment;

import com.example.project_management.dto.ApiResponse;
import com.example.project_management.feature.comment.dto.CommentRequest;
import com.example.project_management.feature.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long taskId, @RequestBody @Valid CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(commentService.createComment(taskId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByTaskId(taskId)));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long taskId, @PathVariable Long commentId) {
        // Validation for commentId belongs to taskId can be added in service layer
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
