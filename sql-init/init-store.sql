    CREATE SCHEMA IF NOT EXISTS store;
    SET search_path TO store;

    -- ====================
    -- USERS TABLE
    -- ====================
    CREATE TABLE IF NOT EXISTS users (
        user_id SERIAL PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        email VARCHAR(100) UNIQUE NOT NULL,
        first_name VARCHAR(100),
        last_name VARCHAR(100),
        bank_account_id VARCHAR(50),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

    -- ====================
    -- PRODUCTS TABLE
    -- ====================
    CREATE TABLE IF NOT EXISTS products (
        product_id SERIAL PRIMARY KEY,
        product_name VARCHAR(200) NOT NULL,
        price DECIMAL(10,2) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

    -- ====================
    -- WAREHOUSES TABLE
    -- ====================
    CREATE TABLE IF NOT EXISTS warehouses (
                                              warehouse_id SERIAL PRIMARY KEY,
                                              warehouse_name VARCHAR(100) NOT NULL,
        address TEXT
        );

    -- ====================
    -- WAREHOUSE_STOCK TABLE
    -- ====================
    CREATE TABLE IF NOT EXISTS warehouse_stock (
        warehouse_id INT NOT NULL REFERENCES warehouses(warehouse_id) ON DELETE CASCADE,
        product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
        quantity INT NOT NULL DEFAULT 0,
        PRIMARY KEY (warehouse_id, product_id)
        );

    -- ====================
    -- ORDERS TABLE
    -- ====================
    CREATE TABLE IF NOT EXISTS orders (
        order_id SERIAL PRIMARY KEY,
        user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
        product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
        quantity INT NOT NULL CHECK (quantity > 0),
        total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
        bank_transaction_id VARCHAR(100),
    --     delivery_id VARCHAR(100),
        warehouse_ids VARCHAR(200),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

    CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
    CREATE INDEX IF NOT EXISTS idx_orders_status  ON orders(status);


    -- ====================
    -- ORDER SAGA TABLE
    -- ====================
    CREATE TABLE IF NOT EXISTS order_saga_state (
        id BIGSERIAL PRIMARY KEY,
        order_id INT UNIQUE REFERENCES orders(order_id) ON DELETE CASCADE,
        status VARCHAR(50) NOT NULL,
        current_step VARCHAR(100) NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL,
        bank_transaction_id VARCHAR(100),
        error_message TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

    CREATE INDEX IF NOT EXISTS idx_saga_order_id ON order_saga_state(order_id);
    CREATE INDEX IF NOT EXISTS idx_saga_status ON order_saga_state(status);