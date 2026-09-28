CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    reference_type VARCHAR(100) NULL,
    reference_id BIGINT NULL,
    correlation_id VARCHAR(255) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,

    PRIMARY KEY (id),

    KEY idx_notifications_user_created (user_id, created_at),
    KEY idx_notifications_user_read (user_id, is_read),
    KEY idx_notifications_reference (reference_type, reference_id),
    KEY idx_notifications_correlation_id (correlation_id)
);