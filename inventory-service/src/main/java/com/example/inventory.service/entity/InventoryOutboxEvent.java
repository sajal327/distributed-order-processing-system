package com.example.inventory.service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventory_outbox",
        indexes = {
                @Index(
                        name = "idx_inventory_outbox_status_next_attempt",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_inventory_outbox_aggregate",
                        columnList = "aggregate_type, aggregate_id"
                ),
                @Index(
                        name = "idx_inventory_outbox_created_at",
                        columnList = "created_at"
                )
        }
)
public class InventoryOutboxEvent {

    public enum Status {
        PENDING,
        PUBLISHED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(
            name = "correlation_id",
            length = 255
    )
    private String correlationId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "routing_key", nullable = false, length = 150)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;

    protected InventoryOutboxEvent() {
    }

    public InventoryOutboxEvent(
            Long eventId,
            String aggregateType,
            Long aggregateId,
            String correlationId,
            String eventType,
            String routingKey,
            String payload
    ) {
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.eventType = eventType;
        this.routingKey = routingKey;
        this.payload = payload;
        this.status = Status.PENDING;
        this.attempts = 0;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPayload() {
        return payload;
    }

    public Status getStatus() {
        return status;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void markFailed(String error, int maxAttempts) {
        this.attempts++;

        this.lastError = error;

        if (this.attempts >= maxAttempts) {
            this.status = Status.FAILED;
            this.nextAttemptAt = null;
        } else {
            this.status = Status.PENDING;

            long delaySeconds =
                    Math.min(300L, 5L * (1L << Math.min(this.attempts - 1, 5)));

            this.nextAttemptAt =
                    LocalDateTime.now().plusSeconds(delaySeconds);
        }
    }
}