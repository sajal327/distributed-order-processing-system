package com.example.inventory.service.messaging;

import java.time.LocalDateTime;

public record InventoryFailedEvent(
        Long eventId,
        String eventType,
        Long orderId,
        Long userId,
        LocalDateTime timestamp,
        String correlationId,
        Long causationId,
        String reason
) {
}