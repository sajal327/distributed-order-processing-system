package com.example.inventory.service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventory_inbox",
        indexes = {
                @Index(
                        name = "idx_inventory_inbox_event_type",
                        columnList = "event_type"
                ),
                @Index(
                        name = "idx_inventory_inbox_aggregate",
                        columnList = "aggregate_id"
                ),
                @Index(
                        name = "idx_inventory_inbox_correlation_id",
                        columnList = "correlation_id"
                )
        }
)
public class InventoryInboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true
    )
    private Long eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 150
    )
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

    protected InventoryInboxEvent() {
    }

    public InventoryInboxEvent(
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