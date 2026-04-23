package com.example.project_management.feature.notification.dto;

import com.example.project_management.feature.notification.Notification;
import com.example.project_management.feature.notification.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String content,
        NotificationType type,
        Long taskId,
        boolean isRead,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getContent(),
                notification.getType(),
                notification.getTaskId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
