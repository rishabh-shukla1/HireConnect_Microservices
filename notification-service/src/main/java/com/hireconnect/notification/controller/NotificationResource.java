package com.hireconnect.notification.controller;

import com.hireconnect.notification.dto.EmailRequest;
import com.hireconnect.notification.dto.NotificationRequest;
import com.hireconnect.notification.dto.NotificationResponse;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest dto) {

        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationService.sendNotification(notification);

        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        notificationService.sendEmailAlert(
            request.getTo(),
            request.getSubject(),
            request.getBody()
        );
        return ResponseEntity.ok("Email sent");
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(
                notificationService.getRecentNotifications(
                        UUID.fromString(userId)
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getByUser(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(notificationService.getByUser(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId) {

        Notification updated = notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(mapToResponse(updated));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllRead(
            @PathVariable UUID userId) {

        notificationService.markAllRead(userId);

        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable UUID notificationId) {

        notificationService.deleteNotification(notificationId);

        return ResponseEntity.ok("Notification deleted successfully");
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }


}