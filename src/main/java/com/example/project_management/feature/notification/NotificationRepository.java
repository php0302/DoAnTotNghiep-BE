package com.example.project_management.feature.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByTaskIdAndType(Long taskId, NotificationType type);
}
