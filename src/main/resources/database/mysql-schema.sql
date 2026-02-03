-- Database: quanlykhachhang
CREATE DATABASE IF NOT EXISTS quanlykhachhang;
USE quanlykhachhang;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role ENUM('ADMIN', 'MANAGER', 'STAFF') NOT NULL DEFAULT 'STAFF',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    customer_type VARCHAR(20),
    date_of_birth DATE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type ENUM('CHECKING', 'SAVINGS') NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5, 2) DEFAULT 0.00,
    term_months INT DEFAULT 0,
    status ENUM('ACTIVE', 'FROZEN', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_date DATETIME,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Indexes for accounts
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Seed Data (Default Users)
-- Password: admin (hash: $2a$10$kQAr8mJQhJ.cZ5.hbIY5YecW.LtyrrUF5GLU1F9WMJavesufiAmlO)
-- Password: manager (hash: $2a$10$A4Ix6g7.bbr9MESwMcIfI.ODC3HlCu.RfRj/DxM5BcU3d9gutbzmy)
-- Password: staff (hash: $2a$10$nLUe1pfVMGyzOv8IAno1iu.2./Aft3g/2hx1O9tooapI6i61gub8a)

INSERT IGNORE INTO users (username, password_hash, full_name, email, role, status) VALUES
('admin', '$2a$10$kQAr8mJQhJ.cZ5.hbIY5YecW.LtyrrUF5GLU1F9WMJavesufiAmlO', 'System Administrator', 'admin@example.com', 'ADMIN', 'ACTIVE'),
('manager', '$2a$10$A4Ix6g7.bbr9MESwMcIfI.ODC3HlCu.RfRj/DxM5BcU3d9gutbzmy', 'Branch Manager', 'manager@example.com', 'MANAGER', 'ACTIVE'),
('staff', '$2a$10$nLUe1pfVMGyzOv8IAno1iu.2./Aft3g/2hx1O9tooapI6i61gub8a', 'Bank Teller', 'staff@example.com', 'STAFF', 'ACTIVE');
