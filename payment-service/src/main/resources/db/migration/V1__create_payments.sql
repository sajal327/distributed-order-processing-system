CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    amount DECIMAL(19, 2) NOT NULL,

    currency VARCHAR(10) NOT NULL DEFAULT 'INR',

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    provider VARCHAR(50),

    provider_payment_id VARCHAR(255),

    correlation_id VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_payments_order_id (order_id),

    KEY idx_payments_status (status),

    KEY idx_payments_correlation_id (correlation_id),

    CONSTRAINT chk_payments_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payments_status
        CHECK (
            status IN (
                'PENDING',
                'PROCESSING',
                'SUCCEEDED',
                'FAILED'
            )
        )
) ENGINE=InnoDB;