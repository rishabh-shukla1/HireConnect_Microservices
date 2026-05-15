package com.hireconnect.application.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String APPLICATION_QUEUE = "application.notification.queue";
    public static final String APPLICATION_EXCHANGE = "application.exchange";

    public static final String APPLICATION_SUBMITTED_ROUTING_KEY = "application.submitted";
    public static final String APPLICATION_STATUS_UPDATED_ROUTING_KEY = "application.status.updated";
    public static final String APPLICATION_WITHDRAWN_ROUTING_KEY = "application.withdrawn";

    @Bean
    public Queue applicationQueue() {
        return new Queue(APPLICATION_QUEUE, true);
    }

    @Bean
    public DirectExchange applicationExchange() {
        return new DirectExchange(APPLICATION_EXCHANGE);
    }

    @Bean
    public Binding submittedBinding() {
        return BindingBuilder.bind(applicationQueue())
                .to(applicationExchange())
                .with(APPLICATION_SUBMITTED_ROUTING_KEY);
    }

    @Bean
    public Binding statusUpdatedBinding() {
        return BindingBuilder.bind(applicationQueue())
                .to(applicationExchange())
                .with(APPLICATION_STATUS_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding withdrawnBinding() {
        return BindingBuilder.bind(applicationQueue())
                .to(applicationExchange())
                .with(APPLICATION_WITHDRAWN_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}