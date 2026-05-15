package com.hireconnect.job.messaging;

import com.hireconnect.job.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobNotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendJobCreated(Long jobId) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.JOB_NOTIFICATION_QUEUE,
                    "Job created with ID: " + jobId);
        } catch (Exception ex) {
            System.err.println("Failed to publish job created event: " + ex.getMessage());
        }
    }

    public void sendJobStatusChanged(Long jobId, String status) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.JOB_NOTIFICATION_QUEUE,
                    "Job " + jobId + " status changed to " + status);
        } catch (Exception ex) {
            System.err.println("Failed to publish job status event: " + ex.getMessage());
        }
    }
}