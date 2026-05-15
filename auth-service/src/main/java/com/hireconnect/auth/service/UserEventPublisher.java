package com.hireconnect.auth.service;

import com.hireconnect.auth.config.RabbitMQConfig;
import com.hireconnect.auth.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreatedEvent(UserCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_CREATED_EXCHANGE,
                RabbitMQConfig.USER_CREATED_ROUTING_KEY,
                event
        );
    }
}