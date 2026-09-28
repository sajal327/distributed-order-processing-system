package com.example.inventory.service.messaging;

import com.example.inventory.service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
public class InventoryReservationResultListener {

    private final RabbitTemplate rabbitTemplate;

    public InventoryReservationResultListener(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleReservationResult(
            InventoryReservationResult result
    ) {

        OrderCreatedEvent orderEvent = result.orderEvent();

        if (result.reserved()) {

            InventoryReservedEvent event =
                    new InventoryReservedEvent(
                            orderEvent.eventId(),
                            "INVENTORY_RESERVED",
                            orderEvent.orderId(),
                            orderEvent.userId(),
                            orderEvent.payload().totalAmount(),
                            "INR",
                            LocalDateTime.now(),
                            orderEvent.correlationId(),
                            orderEvent.eventId()
                    );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EVENTS_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
                    event
            );

        } else {

            InventoryFailedEvent event =
                    new InventoryFailedEvent(
                            orderEvent.eventId(),
                            "INVENTORY_FAILED",
                            orderEvent.orderId(),
                            orderEvent.userId(),
                            LocalDateTime.now(),
                            orderEvent.correlationId(),
                            orderEvent.eventId(),
                            result.reason()
                    );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EVENTS_EXCHANGE,
                    RabbitMQConfig.INVENTORY_FAILED_ROUTING_KEY,
                    event
            );
        }
    }
}