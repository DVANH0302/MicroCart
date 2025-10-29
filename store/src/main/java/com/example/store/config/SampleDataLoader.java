package com.example.store.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SampleDataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("SampleDataLoader starting (resetting seed data)...");

        // First verify and create schema if needed
        Integer schemaExists = jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'store'",
            Integer.class
        );
        log.info("Schema check: store schema {} exist", schemaExists == 1 ? "does" : "does not");

        if (schemaExists == 0) {
            log.info("Creating store schema...");
            jdbc.execute("CREATE SCHEMA store");
        }

        jdbc.execute("SET search_path TO store");

        // Create tables if they don't exist
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS users (
                user_id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                first_name VARCHAR(100),
                last_name VARCHAR(100),
                bank_account_id VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS products (
                product_id SERIAL PRIMARY KEY,
                product_name VARCHAR(200) NOT NULL,
                price DECIMAL(10,2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS warehouses (
                warehouse_id SERIAL PRIMARY KEY,
                warehouse_name VARCHAR(100) NOT NULL,
                address TEXT
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS warehouse_stock (
                warehouse_id INT NOT NULL REFERENCES warehouses(warehouse_id) ON DELETE CASCADE,
                product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
                quantity INT NOT NULL DEFAULT 0,
                PRIMARY KEY (warehouse_id, product_id)
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                order_id SERIAL PRIMARY KEY,
                user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
                quantity INT NOT NULL CHECK (quantity > 0),
                total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
                status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                bank_transaction_id VARCHAR(100),
                warehouse_ids VARCHAR(200),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");

        // Upsert products (don’t need to overwrite price/name each boot, but safe if you want)
        jdbc.update("""
            INSERT INTO store.products(product_id, product_name, price)
            VALUES (1001, 'USB-C Cable', 9.99)
            ON CONFLICT (product_id) DO UPDATE
            SET product_name = EXCLUDED.product_name, price = EXCLUDED.price
        """);
        jdbc.update("""
            INSERT INTO store.products(product_id, product_name, price)
            VALUES (1002, 'Gaming Mouse', 49.90)
            ON CONFLICT (product_id) DO UPDATE
            SET product_name = EXCLUDED.product_name, price = EXCLUDED.price
        """);

        // Upsert warehouses (overwrite name/address to keep them consistent)
        jdbc.update("""
            INSERT INTO store.warehouses(warehouse_id, warehouse_name, address)
            VALUES (1, 'Sydney West DC', '1 Warehouse Ave, Sydney NSW')
            ON CONFLICT (warehouse_id) DO UPDATE
            SET warehouse_name = EXCLUDED.warehouse_name, address = EXCLUDED.address
        """);
        jdbc.update("""
            INSERT INTO store.warehouses(warehouse_id, warehouse_name, address)
            VALUES (2, 'Melbourne DC', '2 Depot Rd, Melbourne VIC')
            ON CONFLICT (warehouse_id) DO UPDATE
            SET warehouse_name = EXCLUDED.warehouse_name, address = EXCLUDED.address
        """);

        // *** Key change: overwrite quantities every time the app starts ***
        jdbc.update("""
            INSERT INTO store.warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (1, 1001, 50)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        jdbc.update("""
            INSERT INTO store.warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (2, 1001, 30)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        jdbc.update("""
            INSERT INTO store.warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (1, 1002, 20)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        jdbc.update("""
            INSERT INTO store.warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (2, 1002, 60)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);


        // insert one user with a deterministic default password for local testing
        log.info("Creating seed data...");

        // Insert products first
        jdbc.update("""
            INSERT INTO products(product_id, product_name, price)
            VALUES (1001, 'USB-C Cable', 9.99)
            ON CONFLICT (product_id) DO UPDATE
            SET product_name = EXCLUDED.product_name, price = EXCLUDED.price
        """);
        log.info("Product 1001 created/updated");

        jdbc.update("""
            INSERT INTO products(product_id, product_name, price)
            VALUES (1002, 'Gaming Mouse', 49.90)
            ON CONFLICT (product_id) DO UPDATE
            SET product_name = EXCLUDED.product_name, price = EXCLUDED.price
        """);
        log.info("Product 1002 created/updated");

        // Insert warehouses
        jdbc.update("""
            INSERT INTO warehouses(warehouse_id, warehouse_name, address)
            VALUES (1, 'Sydney West DC', '1 Warehouse Ave, Sydney NSW')
            ON CONFLICT (warehouse_id) DO UPDATE
            SET warehouse_name = EXCLUDED.warehouse_name, address = EXCLUDED.address
        """);
        log.info("Warehouse 1 created/updated");

        jdbc.update("""
            INSERT INTO warehouses(warehouse_id, warehouse_name, address)
            VALUES (2, 'Melbourne DC', '2 Depot Rd, Melbourne VIC')
            ON CONFLICT (warehouse_id) DO UPDATE
            SET warehouse_name = EXCLUDED.warehouse_name, address = EXCLUDED.address
        """);
        log.info("Warehouse 2 created/updated");

        // Insert warehouse stock
        jdbc.update("""
            INSERT INTO warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (1, 1001, 50)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        log.info("Stock updated: Warehouse 1, Product 1001: 50 units");

        jdbc.update("""
            INSERT INTO warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (2, 1001, 30)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        log.info("Stock updated: Warehouse 2, Product 1001: 30 units");

        jdbc.update("""
            INSERT INTO warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (1, 1002, 20)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        log.info("Stock updated: Warehouse 1, Product 1002: 20 units");

        jdbc.update("""
            INSERT INTO warehouse_stock(warehouse_id, product_id, quantity)
            VALUES (2, 1002, 60)
            ON CONFLICT (warehouse_id, product_id) DO UPDATE
            SET quantity = EXCLUDED.quantity
        """);
        log.info("Stock updated: Warehouse 2, Product 1002: 60 units");

        // Create test user with a deterministic password
        String defaultPassword = passwordEncoder.encode("password123");
        try {
            jdbc.update("""
                INSERT INTO users(username, password_hash, email, first_name, last_name, bank_account_id)
                VALUES (?, ?, 'andy@gmail.com', 'Andy', 'Doan', 'CUST_001')
                ON CONFLICT (username) DO UPDATE
                SET password_hash = EXCLUDED.password_hash,
                    email = EXCLUDED.email,
                    first_name = EXCLUDED.first_name,
                    last_name = EXCLUDED.last_name,
                    bank_account_id = EXCLUDED.bank_account_id
            """, "andy", defaultPassword);

            Integer userId = jdbc.queryForObject(
                "SELECT user_id FROM users WHERE username = 'andy'",
                Integer.class
            );
            log.info("Test user created/updated with ID: {}", userId);

        // insert one order
            // Create test order for the user
            log.info("Creating test order...");
            jdbc.update("""
                INSERT INTO orders(user_id, product_id, quantity, total_amount, status, bank_transaction_id, warehouse_ids)
                VALUES (?, 1001, 10, 1000.00, 'PENDING', 'seed-txn-001', '1,2')
            """, userId);
            log.info("Test order created for user_id: {}", userId);
            log.info("Seed data creation completed successfully");
        } catch (Exception e) {
            log.error("Error creating seed data: {}", e.getMessage());
            throw e;
        }

        log.info("SampleDataLoader finished. Seed stock reset to: 1001 => (1:50, 2:30), 1002 => (1:20, 2:60)");
    }
}
