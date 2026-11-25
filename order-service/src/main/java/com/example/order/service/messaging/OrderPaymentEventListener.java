
package com.example.order.service.messaging;

import com.example.order.service.config.RabbitMQConfig;
import com.example.order.service.service.OrderPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OrderPaymentEventListener {

    private final OrderPaymentService orderPaymentService;
    private final ObjectMapper objectMapper;

    public OrderPaymentEventListener(
            OrderPaymentService orderPaymentService,
            ObjectMapper objectMapper
    ) {
        this.orderPaymentService = orderPaymentService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            queues = RabbitMQConfig.ORDER_PAYMENT_QUEUE
    )
    public void handlePaymentEvent(Message message) {

        try {
            String routingKey =
                    message.getMessageProperties()
                            .getReceivedRoutingKey();

            String payload =
                    new String(
                            message.getBody(),
                            StandardCharsets.UTF_8
                    );

            if (RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY
                    .equals(routingKey)) {

                PaymentCompletedEvent event =
                        objectMapper.readValue(
                                payload,
                                PaymentCompletedEvent.class
                        );

                orderPaymentService.processPaymentCompleted(event);
                return;
            }

            if (RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY
                    .equals(routingKey)) {

                PaymentFailedEvent event =
                        objectMapper.readValue(
                                payload,
                                PaymentFailedEvent.class
                        );

                orderPaymentService.processPaymentFailed(event);
                return;
            }

            throw new IllegalArgumentException(
                    "Unsupported payment routing key: " + routingKey
            );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to process payment event",
                    ex
            );
        }
    }
}
