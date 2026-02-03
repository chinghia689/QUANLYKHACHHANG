-- Script to migrate data from SQLite to MySQL
-- Usage:
-- 1. Export data from SQLite:
--    sqlite3 customer_database.db ".mode insert customers" ".output customers_dump.sql" "select * from customers;" ".exit"
-- 2. Clean up the dump file (remove "TRANSACTION" statements if any, ensure syntax is compatible)
--    Note: SQLite dump uses "INSERT INTO table VALUES(...)" which works for MySQL too.
--    However, verify date formats. SQLite stores dates as strings. MySQL expects 'YYYY-MM-DD HH:MM:SS'.
--    If SQLite dates have 'T' separator (ISO 8601), MySQL 8.0 usually handles it, but safer to replace 'T' with space.

-- 3. Import into MySQL:
--    mysql -u root -p quanlykhachhang < customers_dump.sql

-- Example migration commands (Manual steps):
/*
-- Step 1: Export from SQLite (Linux/Mac)
sqlite3 customer_database.db <<EOF
.headers off
.mode insert customers
.output customers_dump.sql
SELECT id, full_name, phone, email, address, customer_type, date_of_birth, created_date FROM customers;
.exit
EOF

-- Step 2: Fix Date Formats (Optional, if needed)
-- sed -i 's/T/ /g' customers_dump.sql

-- Step 3: Import to MySQL
mysql -u root -p quanlykhachhang < customers_dump.sql
*/
