package com.hireconnect.profile.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String USER_CREATED_EXCHANGE = "user.created.exchange";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE, true);
    }

    @Bean
    public DirectExchange userCreatedExchange() {
        return new DirectExchange(USER_CREATED_EXCHANGE);
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(userCreatedQueue())
                .to(userCreatedExchange())
                .with(USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}