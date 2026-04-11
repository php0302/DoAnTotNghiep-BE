package com.example.project_management.feature.notification.dto;

import com.example.project_management.feature.notification.Notification;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String content,
        boolean isRead,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
