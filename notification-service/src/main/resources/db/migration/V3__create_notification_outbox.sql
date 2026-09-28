CREATE TABLE notification_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    correlation_id VARCHAR(255) NULL,
    event_type VARCHAR(150) NOT NULL,
    routing_key VARCHAR(150) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NULL,
    published_at TIMESTAMP NULL,
    last_error VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_notification_outbox_event_id (event_id),

    KEY idx_notification_outbox_status_next_attempt
        (status, next_attempt_at),

    KEY idx_notification_outbox_aggregate
        (aggregate_type, aggregate_id),

    KEY idx_notification_outbox_created_at
        (created_at),

    KEY idx_notification_outbox_correlation_id
        (correlation_id)
);