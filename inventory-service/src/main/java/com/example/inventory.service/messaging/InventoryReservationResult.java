package com.example.inventory.service.messaging;

public record InventoryReservationResult(
        OrderCreatedEvent orderEvent,
        boolean reserved,
        String reason
) {
}