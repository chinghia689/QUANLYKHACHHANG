-- MySQL Schema for Customer Management Application

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    date_of_birth DATE,
    customer_type VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    INDEX idx_full_name (full_name),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_customer_type (customer_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
