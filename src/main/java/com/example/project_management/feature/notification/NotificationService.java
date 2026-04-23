package com.example.project_management.feature.notification;

import com.example.project_management.feature.notification.dto.NotificationResponse;
import com.example.project_management.feature.user.User;
import java.util.List;

public interface NotificationService {

    /**
     * Lưu notification vào DB và push realtime qua WebSocket.
     *
     * @param user    người nhận
     * @param content nội dung hiển thị
     * @param type    loại sự kiện
     * @param taskId  ID task liên quan (nullable)
     */
    void createAndPush(User user, String content, NotificationType type, Long taskId);

    /** Backward-compat: tạo notification GENERAL không có taskId */
    void createNotification(User user, String content);

    List<NotificationResponse> getMyNotifications();

    void markAsRead(Long notificationId);

    void markAllAsRead();
}
