package com.example.project_management.feature.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    List<CommentMention> findByCommentId(Long commentId);

    boolean existsByCommentIdAndMentionedUserId(Long commentId, Long userId);

    /** Xoá mention khi xoá comment (dùng khi deleteComment) */
    void deleteByCommentId(Long commentId);

    /** Lấy tất cả mention của các comment thuộc 1 task */
    @Query("SELECT cm FROM CommentMention cm WHERE cm.comment.task.id = :taskId")
    List<CommentMention> findByTaskId(@Param("taskId") Long taskId);
}
