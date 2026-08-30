CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,

    amount DECIMAL(19, 2) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    payment_reference VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT uk_payments_order
        UNIQUE (order_id),

    CONSTRAINT uk_payments_reference
        UNIQUE (payment_reference),

    CONSTRAINT chk_payments_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payments_status
        CHECK (
            status IN (
                'PENDING',
                'SUCCESS',
                'FAILED'
            )
        ),

    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status)
);