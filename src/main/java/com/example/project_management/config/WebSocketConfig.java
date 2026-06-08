package com.example.project_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    public WebSocketConfig(WebSocketAuthChannelInterceptor authChannelInterceptor) {
        this.authChannelInterceptor = authChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker xử lý /topic (broadcast) và /queue (user-specific)
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix cho các @MessageMapping trong controller
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix để route đến user cụ thể: /user/{username}/queue/notifications
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Native WebSocket — dùng cho @stomp/stompjs brokerURL (ws://...)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // 2. SockJS fallback — cho browser cũ hoặc SockJS client
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Gắn interceptor xác thực JWT cho mọi frame STOMP đến
        registration.interceptors(authChannelInterceptor);
    }
}
