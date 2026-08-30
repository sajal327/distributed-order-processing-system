-- ─────────────────────────────────────────────────────────────
-- Development seed data
-- ─────────────────────────────────────────────────────────────

-- Users
INSERT INTO users (
    username,
    email,
    password_hash,
    role
)
VALUES
(
    'sajal laha',
    'sajallaha16@gamil.com',
    '$2a$10$development-only-password-hash',
    'USER'
);

-- Categories
INSERT INTO categories (
    name,
    description
)
VALUES
(
    'Electronics',
    'Electronic products'
),
(
    'Books',
    'Books and educational materials'
);

-- Products
INSERT INTO products (
    category_id,
    sku,
    name,
    description,
    price
)
VALUES
(
    (SELECT id FROM categories WHERE name = 'Electronics'),
    'PHONE-001',
    'Smartphone',
    'Development test smartphone',
    29999.00
),
(
    (SELECT id FROM categories WHERE name = 'Electronics'),
    'LAPTOP-001',
    'Laptop',
    'Development test laptop',
    74999.00
),
(
    (SELECT id FROM categories WHERE name = 'Books'),
    'BOOK-001',
    'Java Programming',
    'Development test Java book',
    799.00
);

-- Inventory
INSERT INTO inventory (
    product_id,
    available_quantity,
    reserved_quantity,
    version
)
VALUES
(
    (SELECT id FROM products WHERE sku = 'PHONE-001'),
    100,
    0,
    0
),
(
    (SELECT id FROM products WHERE sku = 'LAPTOP-001'),
    50,
    0,
    0
),
(
    (SELECT id FROM products WHERE sku = 'BOOK-001'),
    200,
    0,
    0
);