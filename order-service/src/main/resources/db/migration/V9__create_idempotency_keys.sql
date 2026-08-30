CREATE TABLE idempotency_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,

    idempotency_key VARCHAR(255) NOT NULL,

    user_id BIGINT NOT NULL,

    request_hash VARCHAR(64) NOT NULL,

    response_status INT,

    response_body JSON,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    expires_at TIMESTAMP NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_idempotency_key_user
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT fk_idempotency_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    INDEX idx_idempotency_expires_at (expires_at),
    INDEX idx_idempotency_created_at (created_at)
);