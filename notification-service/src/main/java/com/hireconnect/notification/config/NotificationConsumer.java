package com.hireconnect.notification.config;

import com.hireconnect.notification.event.ApplicationStatusChangedEvent;
import com.hireconnect.notification.event.InterviewNotificationEvent;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.APPLICATION_QUEUE)
    public void consumeApplicationEvent(ApplicationStatusChangedEvent event) {

        Notification notification = new Notification();
        notification.setUserId(event.getCandidateId());
        notification.setTitle("Application Status Updated");
        notification.setMessage(
                "Your application status is now: " + event.getStatus());

        notificationService.sendNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfig.INTERVIEW_QUEUE)
    public void consumeInterviewEvent(InterviewNotificationEvent event) {

        Notification notification = new Notification();
        notification.setUserId(event.getCandidateId());

        switch (event.getStatus()) {
            case "SCHEDULED" -> {
                notification.setTitle("Interview Scheduled");
                notification.setMessage(event.getMessage());
            }

            case "CONFIRMED" -> {
                notification.setTitle("Interview Confirmed");
                notification.setMessage(event.getMessage());
            }

            case "RESCHEDULED" -> {
                notification.setTitle("Interview Rescheduled");
                notification.setMessage(event.getMessage());
            }

            case "CANCELLED" -> {
                notification.setTitle("Interview Cancelled");
                notification.setMessage(event.getMessage());
            }

            default -> {
                notification.setTitle("Interview Update");
                notification.setMessage(event.getMessage());
            }
        }

        notificationService.sendNotification(notification);
    }
}