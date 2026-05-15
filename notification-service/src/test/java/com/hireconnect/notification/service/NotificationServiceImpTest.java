package com.hireconnect.notification.service;

import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.exception.NotificationNotFoundException;
import com.hireconnect.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationServiceImp.
 * Covers sending notifications, marking as read, deleting,
 * email alerts, and count/query operations.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImpTest {

    /** Mocked JPA repository for Notification persistence. */
    @Mock
    private NotificationRepository notificationRepository;

    /** Mocked Spring mail sender for email dispatch. */
    @Mock
    private JavaMailSender emailSender;

    /** The service under test — uses the mocks above. */
    @InjectMocks
    private NotificationServiceImp notificationService;

    /**
     * Injects the @Value("spring.mail.username") field which
     * would normally be set by Spring's property resolver.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "mailUsername", "test@hireconnect.com");
    }

    // ─── Send Notification ──────────────────────────────────────

    /**
     * Verifies that sendNotification sets createdAt if null,
     * marks the notification as unread, and persists it.
     */
    @Test
    void sendNotification_ShouldSetDefaultsAndSave() {

        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID());
        notification.setUserId(UUID.randomUUID());
        notification.setTitle("New Application");
        notification.setMessage("You received a new application.");
        // createdAt is null — service should fill it

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.sendNotification(notification);

        assertNotNull(result);
        assertFalse(result.isRead()); // should be marked unread
        verify(notificationRepository).save(notification);
    }

    /**
     * Verifies that sendNotification preserves an existing createdAt value.
     */
    @Test
    void sendNotification_WithExistingTimestamp_ShouldPreserveIt() {

        LocalDateTime existingTime = LocalDateTime.of(2026, 1, 1, 10, 0);

        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID());
        notification.setUserId(UUID.randomUUID());
        notification.setTitle("Test");
        notification.setMessage("Test message");
        notification.setCreatedAt(existingTime);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.sendNotification(notification);

        assertEquals(existingTime, result.getCreatedAt());
    }

    // ─── Mark As Read ───────────────────────────────────────────

    /**
     * Verifies markAsRead sets read=true and saves.
     */
    @Test
    void markAsRead_ShouldSetReadTrue() {

        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.markAsRead(notificationId);

        assertTrue(result.isRead());
        verify(notificationRepository).save(notification);
    }

    /**
     * Verifies markAsRead throws NotificationNotFoundException
     * when the notification does not exist.
     */
    @Test
    void markAsRead_NotFound_ShouldThrow() {

        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.markAsRead(notificationId));
    }

    // ─── Mark All Read ──────────────────────────────────────────

    /**
     * Verifies markAllRead flips all unread notifications to read
     * for the given user and batch-saves them.
     */
    @Test
    void markAllRead_ShouldMarkAllUnreadAsRead() {

        UUID userId = UUID.randomUUID();

        Notification n1 = new Notification();
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setRead(false);

        when(notificationRepository.findByUserIdAndRead(userId, false))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllRead(userId);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    // ─── Get By User ────────────────────────────────────────────

    /**
     * Verifies getByUser delegates to the repository correctly.
     */
    @Test
    void getByUser_ShouldReturnNotifications() {

        UUID userId = UUID.randomUUID();
        Notification n = new Notification();
        n.setUserId(userId);

        when(notificationRepository.findByUserId(userId)).thenReturn(List.of(n));

        List<Notification> result = notificationService.getByUser(userId);

        assertEquals(1, result.size());
    }

    // ─── Delete Notification ────────────────────────────────────

    /**
     * Verifies deleteNotification removes the notification if it exists.
     */
    @Test
    void deleteNotification_ShouldDelete() {

        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setNotificationId(notificationId);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId);

        verify(notificationRepository).delete(notification);
    }

    /**
     * Verifies deleteNotification throws when notification not found.
     */
    @Test
    void deleteNotification_NotFound_ShouldThrow() {

        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.deleteNotification(notificationId));
    }

    // ─── Unread Count ───────────────────────────────────────────

    /**
     * Verifies getUnreadCount returns the count from the repository.
     */
    @Test
    void getUnreadCount_ShouldReturnCorrectCount() {

        UUID userId = UUID.randomUUID();

        when(notificationRepository.countByUserIdAndRead(userId, false)).thenReturn(3L);

        long count = notificationService.getUnreadCount(userId);

        assertEquals(3L, count);
    }

    // ─── Send Email Alert ───────────────────────────────────────

    /**
     * Verifies sendEmailAlert dispatches a SimpleMailMessage
     * through the JavaMailSender.
     */
    @Test
    void sendEmailAlert_ShouldSendEmail() {

        notificationService.sendEmailAlert("test@example.com", "Subject", "Body text");

        verify(emailSender).send(any(SimpleMailMessage.class));
    }
}
