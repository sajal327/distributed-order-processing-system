package com.example.payment.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMQConfig {

    // Incoming Inventory events
    public static final String INVENTORY_EVENTS_EXCHANGE = "inventory.events";

    public static final String PAYMENT_INVENTORY_QUEUE =
            "payment.inventory.results";

    public static final String INVENTORY_RESERVED_ROUTING_KEY =
            "inventory.reserved";

    public static final String INVENTORY_FAILED_ROUTING_KEY =
            "inventory.failed";

    // Outgoing Payment events
    public static final String PAYMENT_EVENTS_EXCHANGE =
            "payment.events";

    public static final String PAYMENT_COMPLETED_ROUTING_KEY =
            "payment.completed";

    public static final String PAYMENT_FAILED_ROUTING_KEY =
            "payment.failed";

    @Bean
    public DirectExchange inventoryEventsExchange() {
        return new DirectExchange(
                INVENTORY_EVENTS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue paymentInventoryQueue() {
        return new Queue(
                PAYMENT_INVENTORY_QUEUE,
                true
        );
    }

    @Bean
    public Binding inventoryReservedBinding(
            Queue paymentInventoryQueue,
            @Qualifier("inventoryEventsExchange")
            DirectExchange inventoryEventsExchange
    ) {
        return BindingBuilder
                .bind(paymentInventoryQueue)
                .to(inventoryEventsExchange)
                .with(INVENTORY_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryFailedBinding(
            Queue paymentInventoryQueue,
            @Qualifier("inventoryEventsExchange")
            DirectExchange inventoryEventsExchange
    ) {
        return BindingBuilder
                .bind(paymentInventoryQueue)
                .to(inventoryEventsExchange)
                .with(INVENTORY_FAILED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paymentEventsExchange() {
        return new DirectExchange(
                PAYMENT_EVENTS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory
    ) {
        return new RabbitTemplate(connectionFactory);
    }
}