package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    Notification sendNotification(Notification notification);

    Notification markAsRead(UUID notificationId);

    void markAllRead(UUID userId);

    List<Notification> getByUser(UUID userId);

    void deleteNotification(UUID notificationId);

    void sendEmailAlert(String email, String subject, String body);

    long getUnreadCount(UUID userId);
    
    List<Notification> getRecentNotifications(UUID userId);
}