package com.customer.dao;

import com.customer.model.Account;
import com.customer.model.AccountStatus;
import com.customer.model.AccountType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private final Connection connection;

    public AccountDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Generate new account number (auto-increment from 1001000001)
    public String generateAccountNumber() throws SQLException {
        String sql = "SELECT MAX(account_number) FROM accounts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String maxAccount = rs.getString(1);
                if (maxAccount != null) {
                    try {
                        long nextNum = Long.parseLong(maxAccount) + 1;
                        return String.valueOf(nextNum);
                    } catch (NumberFormatException e) {
                        // Fallback if format is weird
                    }
                }
            }
        }
        return "1001000001";
    }

    // Create
    public void save(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_number, account_type, balance, " +
                "interest_rate, term_months, status, created_date, closed_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, account.getCustomerId());
            pstmt.setString(2, account.getAccountNumber());
            pstmt.setString(3, account.getAccountType().name());
            pstmt.setBigDecimal(4, account.getBalance());
            pstmt.setDouble(5, account.getInterestRate());
            pstmt.setInt(6, account.getTermMonths());
            pstmt.setString(7, account.getStatus().name());
            pstmt.setTimestamp(8, account.getCreatedDate() != null ? Timestamp.valueOf(account.getCreatedDate()) : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(9, account.getClosedDate() != null ? Timestamp.valueOf(account.getClosedDate()) : null);

            pstmt.executeUpdate();

            // Get the generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                account.setId(rs.getLong(1));
            }
        }
    }

    // Read All (with customer name)
    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name as customer_name " +
                "FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id " +
                "ORDER BY a.created_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(extractAccountFromResultSet(rs));
            }
        }

        return accounts;
    }

    // Read by ID
    public Account findById(long id) throws SQLException {
        String sql = "SELECT a.*, c.full_name as customer_name " +
                "FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id " +
                "WHERE a.id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccountFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Read by Customer ID
    public List<Account> findByCustomerId(long customerId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name as customer_name " +
                "FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id " +
                "WHERE a.customer_id = ? " +
                "ORDER BY a.created_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(extractAccountFromResultSet(rs));
                }
            }
        }

        return accounts;
    }

    // Check if customer already has an active account of this type
    public boolean hasAccountOfType(long customerId, AccountType type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts " +
                "WHERE customer_id = ? AND account_type = ? AND status != 'CLOSED'";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, customerId);
            pstmt.setString(2, type.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Update Status (Freeze/Unfreeze/Close)
    public void updateStatus(long id, AccountStatus status) throws SQLException {
        String sql = "UPDATE accounts SET status = ?, closed_date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());

            if (status == AccountStatus.CLOSED) {
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                pstmt.setTimestamp(2, null);
            }

            pstmt.setLong(3, id);
            pstmt.executeUpdate();
        }
    }

    // Update Balance
    public void updateBalance(long id, java.math.BigDecimal balance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, balance);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        }
    }

    // Update with provided connection (for transactions)
    public void update(Account account, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, status = ?, closed_date = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, account.getBalance());
            pstmt.setString(2, account.getStatus().name());
            pstmt.setTimestamp(3, account.getClosedDate() != null ? Timestamp.valueOf(account.getClosedDate()) : null);
            pstmt.setLong(4, account.getId());
            pstmt.executeUpdate();
        }
    }

    // Search and Filter
    public List<Account> search(String keyword, AccountType type, AccountStatus status) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, c.full_name as customer_name " +
                "FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.id " +
                "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (a.account_number LIKE ? OR c.full_name LIKE ?) ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (type != null) {
            sql.append("AND a.account_type = ? ");
            params.add(type.name());
        }

        if (status != null) {
            sql.append("AND a.status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY a.created_date DESC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(extractAccountFromResultSet(rs));
                }
            }
        }

        return accounts;
    }

    // Helper to extract Account from ResultSet
    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setCustomerId(rs.getLong("customer_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setInterestRate(rs.getDouble("interest_rate"));
        account.setTermMonths(rs.getInt("term_months"));
        account.setStatus(AccountStatus.valueOf(rs.getString("status")));

        Timestamp created = rs.getTimestamp("created_date");
        if (created != null) account.setCreatedDate(created.toLocalDateTime());

        Timestamp closed = rs.getTimestamp("closed_date");
        if (closed != null) account.setClosedDate(closed.toLocalDateTime());

        // Transient field
        try {
            String customerName = rs.getString("customer_name");
            if (customerName != null) {
                account.setCustomerName(customerName);
            }
        } catch (SQLException e) {
            // Column might not exist in some queries
        }

        return account;
    }
}
