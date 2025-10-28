CREATE SCHEMA IF NOT EXISTS bank;
SET search_path TO bank;

-- Create bank_accounts table
CREATE TABLE IF NOT EXISTS bank_accounts (
    account_id VARCHAR(50) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_balance CHECK (balance >= 0)
);

-- Create bank_transactions table
CREATE TABLE IF NOT EXISTS bank_transactions (
    transaction_id VARCHAR(100) PRIMARY KEY,
    from_account VARCHAR(50) NOT NULL,
    to_account VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    order_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_amount CHECK (amount > 0),
    CONSTRAINT valid_type CHECK (type IN ('PAYMENT', 'REFUND')),
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT fk_from_account FOREIGN KEY (from_account) REFERENCES bank_accounts(account_id),
    CONSTRAINT fk_to_account FOREIGN KEY (to_account) REFERENCES bank_accounts(account_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_bank_transactions_order_id ON bank_transactions(order_id);
CREATE INDEX idx_bank_transactions_status ON bank_transactions(status);
CREATE INDEX idx_bank_transactions_from_account ON bank_transactions(from_account);
CREATE INDEX idx_bank_transactions_to_account ON bank_transactions(to_account);
CREATE INDEX idx_bank_transactions_type ON bank_transactions(type);
CREATE INDEX idx_bank_transactions_created_at ON bank_transactions(created_at);

-- Create trigger function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for automatic updated_at updates
CREATE TRIGGER update_bank_accounts_updated_at
    BEFORE UPDATE ON bank_accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bank_transactions_updated_at
    BEFORE UPDATE ON bank_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
