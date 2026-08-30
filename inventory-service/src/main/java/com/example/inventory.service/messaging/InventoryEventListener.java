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
    public void handleOrderCreated(OrderCreatedEvent event) {

        try {

            inventoryService.reserveInventory(event);

        } catch (IllegalStateException | IllegalArgumentException ex) {

            inventoryService.markInventoryFailed(
                    event,
                    ex.getMessage()
            );
        }
    }
}

//package com.example.inventory.service.messaging;
//
//import com.example.inventory.service.service.InventoryService;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class InventoryEventListener {
//
//    private final InventoryService inventoryService;
//
//    public InventoryEventListener(InventoryService inventoryService) {
//        this.inventoryService = inventoryService;
//    }
//
//    @RabbitListener(queues = "inventory.orders")
//    public void handleOrderCreated(OrderCreatedEvent event) {
//
//        inventoryService.reserveInventory(event);
//    }
//}