
package com.example.order.service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_inbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_inbox_event_id",
                        columnNames = "event_id"
                )
        }
)
public class OrderInboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "aggregate_id")
    private Long aggregateId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(
            name = "processed_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime processedAt;

    protected OrderInboxEvent() {
    }

    public OrderInboxEvent(
            Long eventId,
            String eventType,
            Long aggregateId,
            String correlationId
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
