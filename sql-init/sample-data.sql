-- Insert test users
INSERT INTO store.users (username, password_hash, email, first_name, last_name, bank_account_id) VALUES
('john_doe', '$2a$10$xPseX7k2cGqhbxX5y1J5K.8h5RlXe1RSKz1Y1XxY1XxY1XxY1X', 'john@example.com', 'John', 'Doe', 'BANK001'),
('jane_smith', '$2a$10$xPseX7k2cGqhbxX5y1J5K.8h5RlXe1RSKz1Y1XxY1XxY1XxY1X', 'jane@example.com', 'Jane', 'Smith', 'BANK002');

-- Insert test products
INSERT INTO store.products (product_name, price) VALUES
('Laptop', 1299.99),
('Smartphone', 699.99),
('Headphones', 199.99),
('Tablet', 499.99);

-- Insert test warehouses
INSERT INTO store.warehouses (warehouse_name, address) VALUES
('Sydney Warehouse', '123 Sydney Road, Sydney, NSW'),
('Melbourne Warehouse', '456 Melbourne Street, Melbourne, VIC'),
('Brisbane Warehouse', '789 Brisbane Ave, Brisbane, QLD');

-- Insert warehouse stock
INSERT INTO store.warehouse_stock (warehouse_id, product_id, quantity) VALUES
(1, 1, 50),  -- 50 Laptops in Sydney
(1, 2, 100), -- 100 Smartphones in Sydney
(2, 1, 30),  -- 30 Laptops in Melbourne
(2, 3, 200), -- 200 Headphones in Melbourne
(3, 2, 75),  -- 75 Smartphones in Brisbane
(3, 4, 150); -- 150 Tablets in Brisbane

-- Insert test orders
INSERT INTO store.orders (user_id, product_id, quantity, total_amount, status, bank_transaction_id, warehouse_ids) VALUES
(1, 1, 1, 1299.99, 'COMPLETED', 'TXN001', '1'),
(1, 2, 2, 1399.98, 'PENDING', NULL, '1'),
(2, 3, 1, 199.99, 'PROCESSING', 'TXN002', '2');