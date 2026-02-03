package com.customer.util;

import com.customer.dao.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    public static void main(String[] args) {
        System.out.println("Starting manual database migration...");

        String createLoansTable =
            "CREATE TABLE IF NOT EXISTS loans (" +
            "    id BIGINT PRIMARY KEY AUTO_INCREMENT," +
            "    customer_id BIGINT NOT NULL," +
            "    loan_account_id BIGINT NULL," +
            "    loan_number VARCHAR(30) NOT NULL UNIQUE," +
            "    principal_amount DECIMAL(15, 2) NOT NULL," +
            "    interest_rate DECIMAL(5, 2) NOT NULL DEFAULT 12.00," +
            "    term_months INT NOT NULL," +
            "    monthly_payment DECIMAL(15, 2) NOT NULL," +
            "    total_paid DECIMAL(15, 2) NOT NULL DEFAULT 0," +
            "    remaining_balance DECIMAL(15, 2) NOT NULL," +
            "    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
            "    purpose TEXT NULL," +
            "    applied_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    approved_date DATETIME NULL," +
            "    approved_by BIGINT NULL," +
            "    approval_note TEXT NULL," +
            "    start_date DATE NULL," +
            "    end_date DATE NULL," +
            "    created_by BIGINT NOT NULL," +
            "    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (customer_id) REFERENCES customers(id)," +
            "    FOREIGN KEY (loan_account_id) REFERENCES accounts(id)," +
            "    FOREIGN KEY (approved_by) REFERENCES users(id)," +
            "    FOREIGN KEY (created_by) REFERENCES users(id)" +
            ");";

        String[] indexes = {
            "CREATE INDEX idx_loans_customer_id ON loans(customer_id)",
            "CREATE INDEX idx_loans_loan_number ON loans(loan_number)",
            "CREATE INDEX idx_loans_status ON loans(status)",
            "CREATE INDEX idx_loans_approved_by ON loans(approved_by)"
        };

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Creating loans table...");
            stmt.execute(createLoansTable);
            System.out.println("Loans table created successfully.");

            System.out.println("Creating indexes...");
            for (String indexSql : indexes) {
                try {
                    stmt.execute(indexSql);
                    System.out.println("Index created: " + indexSql);
                } catch (SQLException e) {
                    System.out.println("Index might already exist: " + e.getMessage());
                }
            }

            System.out.println("Migration completed successfully!");

        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.exit(0);
    }
}
