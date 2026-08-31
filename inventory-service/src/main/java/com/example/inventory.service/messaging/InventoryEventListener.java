package com.example.inventory.service.messaging;

import com.example.inventory.service.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final InventoryService inventoryService;

    public InventoryEventListener(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = "inventory.orders")
    public void handleOrderCreated(
            OrderCreatedEvent event
    ) {

        try {

            inventoryService.processOrderCreated(event);

        } catch (IllegalStateException |
                 IllegalArgumentException ex) {

            inventoryService.processOrderCreatedFailure(
                    event,
                    ex.getMessage()
            );
        }
    }
}