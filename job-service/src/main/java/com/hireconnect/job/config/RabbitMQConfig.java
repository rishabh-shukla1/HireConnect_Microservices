package com.hireconnect.job.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String JOB_NOTIFICATION_QUEUE = "job-notification-queue";

    @Bean
    public Queue jobNotificationQueue() {
        return new Queue(JOB_NOTIFICATION_QUEUE, true);
    }
}