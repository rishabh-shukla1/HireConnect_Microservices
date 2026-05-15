package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.exception.NotificationNotFoundException;
import com.hireconnect.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the NotificationService interface.
 * Handles in-app notification persistence (send, read, delete)
 * and email dispatch via Spring's JavaMailSender.
 */
@Service
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public NotificationServiceImp(NotificationRepository notificationRepository, JavaMailSender emailSender) {
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    /**
     * Persists a new notification. Sets createdAt to now if not provided,
     * and marks it as unread.
     *
     * @param notification the notification entity to save
     * @return the persisted notification
     */
    @Override
    public Notification sendNotification(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }

        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    public Notification markAsRead(UUID notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + notificationId
                        ));

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    @Override
    public void markAllRead(UUID userId) {

        List<Notification> notifications =
                notificationRepository.findByUserIdAndRead(userId, false);

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    public List<Notification> getByUser(UUID userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public void deleteNotification(UUID notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + notificationId
                        ));

        notificationRepository.delete(notification);
    }

    /**
     * Sends a plain-text email using the configured SMTP credentials.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body text
     * @throws RuntimeException if the email fails to send
     */
    @Override
    public void sendEmailAlert(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            emailSender.send(message);

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Failed to send email: " + ex.getMessage()
            );
        }
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
    
    @Override
    public List<Notification> getRecentNotifications(UUID userId) {
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
    }

  


}