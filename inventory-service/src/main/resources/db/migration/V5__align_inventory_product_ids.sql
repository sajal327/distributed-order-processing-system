-- Align inventory product IDs with Order Service product IDs.
-- Preserve existing inventory records.

INSERT INTO inventory (
    product_id,
    available_quantity,
    reserved_quantity,
    version
)
VALUES
    (1, 100, 0, 0),
    (2, 50, 0, 0),
    (3, 200, 0, 0);