CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    quantity INT NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price >= 0),

    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),

    CONSTRAINT uk_order_items_order_product
        UNIQUE (order_id, product_id)
);