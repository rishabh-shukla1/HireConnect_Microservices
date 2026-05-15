package com.hireconnect.notification.repository;

import com.hireconnect.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserId(UUID userId);

    List<Notification> findByUserIdAndRead(UUID userId, boolean read);

    long countByUserIdAndRead(UUID userId, boolean read);

    void deleteByNotificationId(UUID notificationId);

    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
}