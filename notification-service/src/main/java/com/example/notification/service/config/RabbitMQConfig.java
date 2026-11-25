package com.example.notification.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EVENTS_EXCHANGE =
            "payment.events";

    public static final String PAYMENT_NOTIFICATION_QUEUE =
            "notification.payment.events";

    public static final String PAYMENT_COMPLETED_ROUTING_KEY =
            "payment.completed";

    public static final String PAYMENT_FAILED_ROUTING_KEY =
            "payment.failed";

    @Bean
    public DirectExchange paymentEventsExchange() {
        return new DirectExchange(
                PAYMENT_EVENTS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue paymentNotificationQueue() {
        return new Queue(
                PAYMENT_NOTIFICATION_QUEUE,
                true
        );
    }

    @Bean
    public Binding paymentCompletedBinding(
            Queue paymentNotificationQueue,
            @Qualifier("paymentEventsExchange")
            DirectExchange paymentEventsExchange
    ) {
        return BindingBuilder
                .bind(paymentNotificationQueue)
                .to(paymentEventsExchange)
                .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding(
            Queue paymentNotificationQueue,
            @Qualifier("paymentEventsExchange")
            DirectExchange paymentEventsExchange
    ) {
        return BindingBuilder
                .bind(paymentNotificationQueue)
                .to(paymentEventsExchange)
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }
    @Bean
    public DirectExchange notificationEventsExchange() {
        return new DirectExchange(
                "notification.events",
                true,
                false
        );
    }
}