
package com.example.order.service.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentFailedEvent(
        Long eventId,
        String eventType,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String currency,
        LocalDateTime timestamp,
        String correlationId,
        Long causationId,
        String reason
) {
}
