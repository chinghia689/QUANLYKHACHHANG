-- SQLite Schema for Customer Management Application

CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    date_of_birth TEXT,
    customer_type TEXT NOT NULL,
    created_date TEXT NOT NULL
);

-- Separate index creation for SQLite
CREATE INDEX IF NOT EXISTS idx_full_name ON customers (full_name);
CREATE INDEX IF NOT EXISTS idx_phone ON customers (phone);
CREATE INDEX IF NOT EXISTS idx_email ON customers (email);
CREATE INDEX IF NOT EXISTS idx_customer_type ON customers (customer_type);
