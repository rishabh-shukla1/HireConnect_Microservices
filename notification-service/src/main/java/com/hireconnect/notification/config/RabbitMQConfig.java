package com.hireconnect.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String APPLICATION_QUEUE = "application.notification.queue";
    public static final String INTERVIEW_QUEUE = "interview.notification.queue";

    public static final String APPLICATION_EXCHANGE = "application.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    @Bean
    public Queue applicationQueue() {
        return new Queue(APPLICATION_QUEUE, true);
    }

    @Bean
    public Queue interviewQueue() {
        return new Queue(INTERVIEW_QUEUE, true);
    }

    @Bean
    public DirectExchange applicationExchange() {
        return new DirectExchange(APPLICATION_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding applicationSubmittedBinding() {
        return BindingBuilder.bind(applicationQueue())
                .to(applicationExchange())
                .with("application.submitted");
    }

    @Bean
    public Binding applicationStatusBinding() {
        return BindingBuilder.bind(applicationQueue())
                .to(applicationExchange())
                .with("application.status.updated");
    }

    @Bean
    public Binding applicationWithdrawnBinding() {
        return BindingBuilder.bind(applicationQueue())
                .to(applicationExchange())
                .with("application.withdrawn");
    }

    @Bean
    public Binding interviewBinding() {
        return BindingBuilder.bind(interviewQueue())
                .to(notificationExchange())
                .with("interview.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}