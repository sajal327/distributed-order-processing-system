package com.example.payment.service.listener;

import com.example.payment.service.messaging.InventoryFailedEvent;
import com.example.payment.service.messaging.InventoryReservedEvent;
import com.example.payment.service.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentEventListener(
            PaymentService paymentService,
            ObjectMapper objectMapper
    ) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            queues = "payment.inventory.results"
    )
    public void handleInventoryEvent(Message message) {

        try {
            String routingKey =
                    message.getMessageProperties().getReceivedRoutingKey();

            String payload =
                    new String(message.getBody());

            if ("inventory.reserved".equals(routingKey)) {

                InventoryReservedEvent event =
                        objectMapper.readValue(
                                payload,
                                InventoryReservedEvent.class
                        );

                paymentService.processInventoryReserved(event);

                return;
            }

            if ("inventory.failed".equals(routingKey)) {

                InventoryFailedEvent event =
                        objectMapper.readValue(
                                payload,
                                InventoryFailedEvent.class
                        );

                paymentService.processInventoryFailed(event);

                return;
            }

            throw new IllegalArgumentException(
                    "Unsupported inventory routing key: " + routingKey
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to process inventory event",
                    ex
            );
        }
    }
}