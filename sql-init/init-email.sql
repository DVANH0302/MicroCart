CREATE SCHEMA IF NOT EXISTS email;
SET search_path TO email;

CREATE TABLE IF NOT EXISTS emails (
    id SERIAL PRIMARY KEY,
    recipient VARCHAR(100),
    subject VARCHAR(255),
    body TEXT,
    type varchar(50),
    status varchar(20),
    reference_id VARCHAR(20),
    created_at TIMESTAMP
    );