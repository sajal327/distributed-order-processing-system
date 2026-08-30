package com.example.order.service.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCreatedEvent {

    private Long eventId;
    private String eventType;
    private Long orderId;
    private Long userId;
    private LocalDateTime timestamp;
    private String correlationId;
    private Payload payload;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(
            Long eventId,
            String eventType,
            Long orderId,
            Long userId,
            LocalDateTime timestamp,
            String correlationId,
            Payload payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.payload = payload;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Payload getPayload() {
        return payload;
    }

    public static class Payload {

        private Long orderId;
        private Long userId;
        private String status;
        private BigDecimal totalAmount;
        private List<Item> items;

        public Payload() {
        }

        public Payload(
                Long orderId,
                Long userId,
                String status,
                BigDecimal totalAmount,
                List<Item> items
        ) {
            this.orderId = orderId;
            this.userId = userId;
            this.status = status;
            this.totalAmount = totalAmount;
            this.items = items;
        }

        public Long getOrderId() {
            return orderId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getStatus() {
            return status;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public List<Item> getItems() {
            return items;
        }

        public static class Item {

            private Long productId;
            private Integer quantity;
            private BigDecimal unitPrice;

            public Item() {
            }

            public Item(
                    Long productId,
                    Integer quantity,
                    BigDecimal unitPrice
            ) {
                this.productId = productId;
                this.quantity = quantity;
                this.unitPrice = unitPrice;
            }

            public Long getProductId() {
                return productId;
            }

            public Integer getQuantity() {
                return quantity;
            }

            public BigDecimal getUnitPrice() {
                return unitPrice;
            }
        }
    }
}