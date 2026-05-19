package com.example.project_management.feature.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service tập trung broadcast realtime events qua WebSocket.
 * Inject vào TaskServiceImpl, CommentServiceImpl để gọi sau khi lưu DB.
 */
@Service
public class WebSocketBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketBroadcastService.class);

    /** Prefix topic cho project — client subscribe: /topic/project.{id} */
    private static final String PROJECT_TOPIC_PREFIX = "/topic/project.";

    /** Topic chung cho Admin — client subscribe: /topic/admin */
    private static final String ADMIN_TOPIC = "/topic/admin";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast message tới mọi subscriber đang xem project này.
     *
     * @param projectId ID của project
     * @param message   Payload (RealtimeMessage)
     */
    public void broadcastToProject(Long projectId, RealtimeMessage message) {
        String destination = PROJECT_TOPIC_PREFIX + projectId;
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("[WS Broadcast] {} → {} (actor={})", message.type(), destination, message.actorId());
        } catch (Exception e) {
            log.warn("[WS Broadcast] Failed to send to {}: {}", destination, e.getMessage());
        }
    }

    /**
     * Broadcast message tới tất cả Admin đang subscribe /topic/admin.
     * Dùng cho các sự kiện quản trị: user đổi mật khẩu, v.v.
     *
     * @param message Payload (RealtimeMessage)
     */
    public void broadcastToAdmins(RealtimeMessage message) {
        try {
            messagingTemplate.convertAndSend(ADMIN_TOPIC, message);
            log.debug("[WS Broadcast] {} → {} (actor={})", message.type(), ADMIN_TOPIC, message.actorId());
        } catch (Exception e) {
            log.warn("[WS Broadcast] Failed to send to {}: {}", ADMIN_TOPIC, e.getMessage());
        }
    }
}
