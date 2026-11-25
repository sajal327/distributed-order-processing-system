CREATE TABLE order_inbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    aggregate_id BIGINT NULL,
    correlation_id VARCHAR(255) NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_order_inbox_event_id (event_id),

    INDEX idx_order_inbox_event_type (event_type),
    INDEX idx_order_inbox_aggregate (aggregate_id),
    INDEX idx_order_inbox_correlation_id (correlation_id)
);