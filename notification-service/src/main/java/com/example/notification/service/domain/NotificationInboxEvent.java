package com.example.notification.service.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_inbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_inbox_event_id",
                        columnNames = "event_id"
                )
        }
)
public class NotificationInboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "aggregate_id")
    private Long aggregateId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    protected NotificationInboxEvent() {
    }

    public NotificationInboxEvent(
            Long eventId,
            String eventType,
            Long aggregateId,
            String correlationId
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.processedAt = LocalDateTime.now();
    }
}