ALTER TABLE inventory_outbox
    ADD COLUMN correlation_id VARCHAR(255) NULL
    AFTER aggregate_id;

CREATE INDEX idx_inventory_outbox_correlation_id
    ON inventory_outbox (correlation_id);