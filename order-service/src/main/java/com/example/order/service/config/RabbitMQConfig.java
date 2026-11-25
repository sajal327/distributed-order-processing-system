
package com.example.order.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Outgoing Order events
    public static final String ORDER_EXCHANGE = "order.events";
    public static final String INVENTORY_QUEUE = "inventory.orders";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // Incoming Payment events
    public static final String PAYMENT_EVENTS_EXCHANGE = "payment.events";
    public static final String ORDER_PAYMENT_QUEUE = "order.payment.results";

    public static final String PAYMENT_COMPLETED_ROUTING_KEY =
            "payment.completed";

    public static final String PAYMENT_FAILED_ROUTING_KEY =
            "payment.failed";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(
                ORDER_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue inventoryOrderQueue() {
        return new Queue(INVENTORY_QUEUE, true);
    }

    @Bean
    public Binding inventoryOrderBinding(
            @Qualifier("inventoryOrderQueue") Queue inventoryOrderQueue,
            @Qualifier("orderExchange") DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(inventoryOrderQueue)
                .to(orderExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    // Payment event exchange
    @Bean
    public DirectExchange paymentEventsExchange() {
        return new DirectExchange(
                PAYMENT_EVENTS_EXCHANGE,
                true,
                false
        );
    }

    // Queue for payment results
    @Bean
    public Queue orderPaymentQueue() {
        return new Queue(ORDER_PAYMENT_QUEUE, true);
    }

    @Bean
    public Binding paymentCompletedBinding(
            @Qualifier("orderPaymentQueue") Queue orderPaymentQueue,
            @Qualifier("paymentEventsExchange")
            DirectExchange paymentEventsExchange
    ) {
        return BindingBuilder
                .bind(orderPaymentQueue)
                .to(paymentEventsExchange)
                .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding(
            @Qualifier("orderPaymentQueue") Queue orderPaymentQueue,
            @Qualifier("paymentEventsExchange")
            DirectExchange paymentEventsExchange
    ) {
        return BindingBuilder
                .bind(orderPaymentQueue)
                .to(paymentEventsExchange)
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(
            ObjectMapper objectMapper
    ) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}



//package com.example.order.service.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.DirectExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    public static final String ORDER_EXCHANGE = "order.events";
//
//    public static final String INVENTORY_QUEUE = "inventory.orders";
//
//    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
//
//    @Bean
//    public DirectExchange orderExchange() {
//        return new DirectExchange(
//                ORDER_EXCHANGE,
//                true,
//                false
//        );
//    }
//
//    @Bean
//    public Queue inventoryOrderQueue() {
//        return new Queue(
//                INVENTORY_QUEUE,
//                true
//        );
//    }
//
//    @Bean
//    public Binding inventoryOrderBinding(
//            Queue inventoryOrderQueue,
//            DirectExchange orderExchange
//    ) {
//        return BindingBuilder
//                .bind(inventoryOrderQueue)
//                .to(orderExchange)
//                .with(ORDER_CREATED_ROUTING_KEY);
//    }
//
//    @Bean
//    public MessageConverter rabbitMessageConverter(
//            ObjectMapper objectMapper
//    ) {
//        return new Jackson2JsonMessageConverter(objectMapper);
//    }
//}