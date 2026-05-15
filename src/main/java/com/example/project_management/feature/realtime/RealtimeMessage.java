package com.example.project_management.feature.realtime;

import java.time.Instant;

/**
 * Wrapper chuẩn cho mọi realtime message gửi qua WebSocket.
 * Client dùng trường "type" để route xử lý tương ứng.
 *
 * Topics:
 *  - /topic/project.{projectId}  → broadcast cho mọi member đang xem project
 *  - /user/queue/notifications    → push notification đến đúng user
 */
public record RealtimeMessage(
        String type,       // TASK_CREATED | TASK_UPDATED | TASK_STATUS_CHANGED | TASK_DELETED | COMMENT_CREATED
        Long   projectId,  // Project liên quan (để client biết scope)
        Long   actorId,    // ID của user thực hiện hành động (để client loại bỏ self-events)
        String actorName,  // Tên hiển thị của actor
        Instant timestamp, // Thời điểm sự kiện
        Object data        // Payload cụ thể — TaskResponse, CommentResponse, v.v.
) {
    /** Factory helpers */
    public static RealtimeMessage of(String type, Long projectId, Long actorId, String actorName, Object data) {
        return new RealtimeMessage(type, projectId, actorId, actorName, Instant.now(), data);
    }
}
