package com.customer.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {
    // SQLite Configuration
    private static final String DB_FILE = "customer_database.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            // Load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");

            // Connect to SQLite
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
            System.out.println("✅ SQLite Database connected successfully!");
            System.out.println("📍 Database file: " + DB_FILE);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void initializeDatabase() {
        try {
            // Read schema.sql from resources
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("database/schema.sql");

            if (is == null) {
                System.err.println("⚠️  schema.sql not found in resources");
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Execute the schema
            try (Statement statement = connection.createStatement()) {
                // Split by semicolon and execute each statement
                String[] statements = sql.split(";");
                for (String stmt : statements) {
                    if (!stmt.trim().isEmpty()) {
                        statement.execute(stmt.trim());
                    }
                }
            }

            System.out.println("✅ Database schema initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get connection parameters (for display purposes)
    public static String getConnectionInfo() {
        return "SQLite @ " + DB_FILE;
    }
}
