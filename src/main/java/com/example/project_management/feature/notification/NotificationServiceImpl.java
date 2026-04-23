package com.example.project_management.feature.notification;

import com.example.project_management.exception.ResourceNotFoundException;
import com.example.project_management.feature.notification.dto.NotificationResponse;
import com.example.project_management.feature.user.User;
import com.example.project_management.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    /** Destination user lắng nghe trên frontend */
    private static final String NOTIFICATION_DESTINATION = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ── Core method: lưu DB + push WebSocket ─────────────────────────────────

    @Override
    @Transactional
    public void createAndPush(User user, String content, NotificationType type, Long taskId) {
        // 1. Lưu vào DB
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setContent(content);
        notification.setType(type);
        notification.setTaskId(taskId);
        Notification saved = notificationRepository.save(notification);

        // 2. Push realtime đến đúng user qua WebSocket
        // Spring dùng email (subject JWT) làm key định tuyến
        String userEmail = user.getEmail();
        NotificationResponse payload = NotificationResponse.fromEntity(saved);
        try {
            messagingTemplate.convertAndSendToUser(userEmail, NOTIFICATION_DESTINATION, payload);
            log.debug("Pushed notification to {}: {}", userEmail, content);
        } catch (Exception e) {
            // User offline — không sao, họ sẽ load từ DB khi login lại
            log.debug("User {} is offline, notification saved to DB only", userEmail);
        }
    }

    // ── Backward-compat ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void createNotification(User user, String content) {
        createAndPush(user, content, NotificationType.GENERAL, null);
    }

    // ── Query / Update ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User", "context", "current user"));
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
