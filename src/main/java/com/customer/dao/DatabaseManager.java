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
    // MySQL/XAMPP Configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "customer_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // XAMPP default: empty password

    private static final String DB_URL = String.format(
            "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            DB_HOST, DB_PORT, DB_NAME);

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeDatabase();
            System.out.println("✅ MySQL Database connected successfully!");
            System.out.println("📍 Database: " + DB_NAME + " on " + DB_HOST + ":" + DB_PORT);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("💡 Make sure XAMPP MySQL is running!");
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
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
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
        return "MySQL @ " + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    }
}
