-- Orders table schema
-- NOTE: No index on order_number column - this will cause slow lookups!

CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    customer_email TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10,2),
    shipping_address TEXT,
    order_metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items table - linked by order_number
-- NOTE: No index on order_number here either - JOINs will be slow!
CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number TEXT NOT NULL,
    product_sku TEXT NOT NULL,
    product_name TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index on id is automatic (PRIMARY KEY)
-- INTENTIONALLY NOT ADDING: CREATE INDEX idx_orders_order_number ON orders(order_number);
-- INTENTIONALLY NOT ADDING: CREATE INDEX idx_order_items_order_number ON order_items(order_number);
