package com.example.notification.service.listener;

import com.example.notification.service.messaging.PaymentCompletedEvent;
import com.example.notification.service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(
            NotificationService notificationService,
            ObjectMapper objectMapper
    ) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            queues = "notification.payment.events"
    )
    public void handlePaymentEvent(Message message) {

        try {

            String routingKey =
                    message.getMessageProperties()
                            .getReceivedRoutingKey();

            String payload =
                    new String(message.getBody());

            if ("payment.completed".equals(routingKey)) {

                PaymentCompletedEvent event =
                        objectMapper.readValue(
                                payload,
                                PaymentCompletedEvent.class
                        );

                notificationService
                        .processPaymentCompleted(event);

                return;
            }

            throw new IllegalArgumentException(
                    "Unsupported payment routing key: "
                            + routingKey
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to process payment event",
                    ex
            );
        }
    }
}