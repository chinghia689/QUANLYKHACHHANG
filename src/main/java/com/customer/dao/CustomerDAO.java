package com.customer.dao;

import com.customer.model.Customer;
import com.customer.model.CustomerType;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDAO {
    private final Connection connection;

    public CustomerDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Create
    public void save(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, phone, email, address, date_of_birth, customer_type, created_date) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : null);
            pstmt.setString(6, customer.getCustomerType().name());
            pstmt.setString(7, customer.getCreatedDate().toString());

            pstmt.executeUpdate();

            // Get the generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                customer.setId(rs.getLong(1));
            }
        }
    }

    // Read All
    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY created_date DESC";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        }

        return customers;
    }

    // Read by ID
    public Customer findById(long id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Update
    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ?, address = ?, " +
                "date_of_birth = ?, customer_type = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : null);
            pstmt.setString(6, customer.getCustomerType().name());
            pstmt.setLong(7, customer.getId());

            pstmt.executeUpdate();
        }
    }

    // Delete
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    // Search by keyword (name, phone, or email)
    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE " +
                "full_name LIKE ? OR phone LIKE ? OR email LIKE ? " +
                "ORDER BY created_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(extractCustomerFromResultSet(rs));
                }
            }
        }

        return customers;
    }

    // Find by Customer Type
    public List<Customer> findByType(CustomerType type) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE customer_type = ? ORDER BY created_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(extractCustomerFromResultSet(rs));
                }
            }
        }

        return customers;
    }

    // Helper method to extract Customer from ResultSet
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String fullName = rs.getString("full_name");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        String address = rs.getString("address");

        String dobString = rs.getString("date_of_birth");
        LocalDate dateOfBirth = dobString != null ? LocalDate.parse(dobString) : null;

        CustomerType customerType = CustomerType.valueOf(rs.getString("customer_type"));

        String createdDateString = rs.getString("created_date");
        LocalDateTime createdDate = LocalDateTime.parse(createdDateString);

        return new Customer(id, fullName, phone, email, address, dateOfBirth, customerType, createdDate);
    }

    // ========== STATISTICS METHODS ==========

    /**
     * Count total number of customers.
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM customers";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Count customers by type.
     */
    public int countByType(CustomerType type) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM customers WHERE customer_type = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Count new customers added this month.
     */
    public int countNewThisMonth() throws SQLException {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        String sql = "SELECT COUNT(*) as total FROM customers WHERE created_date >= ? AND created_date <= ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startOfMonth.toString());
            pstmt.setString(2, endOfMonth.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Get distribution of customers by type (for PieChart).
     */
    public Map<CustomerType, Integer> getTypeDistribution() throws SQLException {
        Map<CustomerType, Integer> distribution = new HashMap<>();

        // Initialize all types with 0
        for (CustomerType type : CustomerType.values()) {
            distribution.put(type, 0);
        }

        String sql = "SELECT customer_type, COUNT(*) as count FROM customers GROUP BY customer_type";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String typeName = rs.getString("customer_type");
                int count = rs.getInt("count");
                try {
                    CustomerType type = CustomerType.valueOf(typeName);
                    distribution.put(type, count);
                } catch (IllegalArgumentException e) {
                    // Skip unknown types
                }
            }
        }

        return distribution;
    }
}
