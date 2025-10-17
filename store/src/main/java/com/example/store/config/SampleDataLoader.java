package com.example.store.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SampleDataLoader implements CommandLineRunner{
    
    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        log.info("SampleDataLoader starting (resetting seed data)...");
        // Always qualify with schema so search_path doesn't matter
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS store");

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

        log.info("SampleDataLoader finished. Seed stock reset to: 1001 => (1:50, 2:30), 1002 => (1:20, 2:60)");
    }
}
