package com.example.project_management.feature.notification;

import com.example.project_management.feature.notification.dto.NotificationResponse;
import com.example.project_management.feature.user.User;
import java.util.List;

public interface NotificationService {
    void createNotification(User user, String content);
    List<NotificationResponse> getMyNotifications();
    void markAsRead(Long notificationId);
}
