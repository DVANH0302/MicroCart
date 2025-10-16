CREATE SCHEMA IF NOT EXISTS delivery;
SET search_path TO delivery;

-- ==============================
-- DELIVERIES TABLE
-- ==============================
CREATE TABLE IF NOT EXISTS deliveries (
    delivery_id VARCHAR(100) PRIMARY KEY,
    order_id INT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    warehouse_ids VARCHAR(200),
    status VARCHAR(50) DEFAULT 'REQUESTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- ==============================
-- INDEXES
-- ==============================
CREATE INDEX IF NOT EXISTS idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);


-- ==============================
-- SAFETY CONSTRAINTS
-- ==============================
-- Each order can only have one delivery record
ALTER TABLE deliveries
    ADD CONSTRAINT unique_order_id UNIQUE (order_id);