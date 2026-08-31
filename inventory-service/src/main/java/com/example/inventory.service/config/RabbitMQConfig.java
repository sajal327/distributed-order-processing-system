package com.example.inventory.service.config;

import com.example.inventory.service.messaging.OrderCreatedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Incoming Order events
    public static final String ORDER_EVENTS_EXCHANGE = "order.events";
    public static final String INVENTORY_QUEUE = "inventory.orders";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // Outgoing Inventory events
    public static final String INVENTORY_EVENTS_EXCHANGE = "inventory.events";
    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";
    public static final String INVENTORY_FAILED_ROUTING_KEY = "inventory.failed";

    @Bean
    public DirectExchange orderEventsExchange() {
        return new DirectExchange(
                ORDER_EVENTS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue inventoryOrdersQueue() {
        return new Queue(
                INVENTORY_QUEUE,
                true
        );
    }

    @Bean
    public Binding inventoryOrderCreatedBinding(
            Queue inventoryOrdersQueue,
            @Qualifier("orderEventsExchange")
            DirectExchange orderEventsExchange
    ) {
        return BindingBuilder
                .bind(inventoryOrdersQueue)
                .to(orderEventsExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange inventoryEventsExchange() {
        return new DirectExchange(
                INVENTORY_EVENTS_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {

        DefaultJackson2JavaTypeMapper typeMapper =
                new DefaultJackson2JavaTypeMapper();

        typeMapper.setIdClassMapping(
                Map.of(
                        "com.example.order.service.messaging.OrderCreatedEvent",
                        OrderCreatedEvent.class
                )
        );

        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter();

        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);

        return factory;
    }
}