package com.example.notification.service.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_outbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_outbox_event_id",
                        columnNames = "event_id"
                )
        }
)
public class NotificationOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "routing_key", nullable = false, length = 150)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "JSON")
    private String payload;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NotificationOutboxEvent() {
    }

    public NotificationOutboxEvent(
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
        this.status = "PENDING";
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getAggregateType() {
        return aggregateType;
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

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
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

    public void markPublished() {
        this.status = "PUBLISHED";
        this.publishedAt = LocalDateTime.now();
        this.lastError = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(
            String error,
            LocalDateTime nextAttemptAt
    ) {
        this.status = "PENDING";
        this.attempts++;
        this.lastError = error;
        this.nextAttemptAt = nextAttemptAt;
        this.updatedAt = LocalDateTime.now();
    }
}