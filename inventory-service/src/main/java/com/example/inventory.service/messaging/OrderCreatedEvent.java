package com.example.inventory.service.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        Long eventId,
        String eventType,
        Long orderId,
        Long userId,
        LocalDateTime timestamp,
        String correlationId,
        Payload payload
) {

    public record Payload(
            Long orderId,
            Long userId,
            String status,
            BigDecimal totalAmount,
            List<Item> items
    ) {
    }

    public record Item(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {
    }
}