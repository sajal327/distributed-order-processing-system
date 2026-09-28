package com.example.inventory.service.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryReservedEvent(
        Long eventId,
        String eventType,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String currency,
        LocalDateTime timestamp,
        String correlationId,
        Long causationId
) {
}