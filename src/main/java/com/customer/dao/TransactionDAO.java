package com.customer.dao;

import com.customer.model.Transaction;
import com.customer.model.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionDAO {

    public void save(Transaction transaction) throws SQLException {
        if (transaction.getReferenceNumber() == null || transaction.getReferenceNumber().isEmpty()) {
            transaction.setReferenceNumber(generateReferenceNumber());
        }
        if (transaction.getCreatedDate() == null) {
            transaction.setCreatedDate(LocalDateTime.now());
        }

        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, target_account_id, " +
                "balance_after, description, reference_number, created_by, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Do not close shared connection
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType().name());
            stmt.setBigDecimal(3, transaction.getAmount());

            if (transaction.getTargetAccountId() > 0) {
                stmt.setLong(4, transaction.getTargetAccountId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.setBigDecimal(5, transaction.getBalanceAfter());
            stmt.setString(6, transaction.getDescription());
            stmt.setString(7, transaction.getReferenceNumber());
            stmt.setLong(8, transaction.getCreatedBy());
            stmt.setTimestamp(9, Timestamp.valueOf(transaction.getCreatedDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
    }

    // Method to save transaction within an existing connection (for transactional atomicity)
    public void save(Transaction transaction, Connection conn) throws SQLException {
        if (transaction.getReferenceNumber() == null || transaction.getReferenceNumber().isEmpty()) {
            transaction.setReferenceNumber(generateReferenceNumber());
        }
        if (transaction.getCreatedDate() == null) {
            transaction.setCreatedDate(LocalDateTime.now());
        }

        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, target_account_id, " +
                "balance_after, description, reference_number, created_by, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType().name());
            stmt.setBigDecimal(3, transaction.getAmount());

            if (transaction.getTargetAccountId() > 0) {
                stmt.setLong(4, transaction.getTargetAccountId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.setBigDecimal(5, transaction.getBalanceAfter());
            stmt.setString(6, transaction.getDescription());
            stmt.setString(7, transaction.getReferenceNumber());
            stmt.setLong(8, transaction.getCreatedBy());
            stmt.setTimestamp(9, Timestamp.valueOf(transaction.getCreatedDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
    }

    public List<Transaction> findByAccountId(long accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, a.account_number, ta.account_number as target_account_number, u.full_name as created_by_name " +
                "FROM transactions t " +
                "LEFT JOIN accounts a ON t.account_id = a.id " +
                "LEFT JOIN accounts ta ON t.target_account_id = ta.id " +
                "LEFT JOIN users u ON t.created_by = u.id " +
                "WHERE t.account_id = ? " +
                "ORDER BY t.created_date DESC";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findByDateRange(long accountId, LocalDateTime from, LocalDateTime to) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, a.account_number, ta.account_number as target_account_number, u.full_name as created_by_name " +
                "FROM transactions t " +
                "LEFT JOIN accounts a ON t.account_id = a.id " +
                "LEFT JOIN accounts ta ON t.target_account_id = ta.id " +
                "LEFT JOIN users u ON t.created_by = u.id " +
                "WHERE t.account_id = ? AND t.created_date BETWEEN ? AND ? " +
                "ORDER BY t.created_date DESC";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);
            stmt.setTimestamp(2, Timestamp.valueOf(from));
            stmt.setTimestamp(3, Timestamp.valueOf(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    public Transaction findByReferenceNumber(String referenceNumber) throws SQLException {
        String sql = "SELECT t.*, a.account_number, ta.account_number as target_account_number, u.full_name as created_by_name " +
                "FROM transactions t " +
                "LEFT JOIN accounts a ON t.account_id = a.id " +
                "LEFT JOIN accounts ta ON t.target_account_id = ta.id " +
                "LEFT JOIN users u ON t.created_by = u.id " +
                "WHERE t.reference_number = ?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        }
        return null;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setAccountId(rs.getLong("account_id"));
        transaction.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setTargetAccountId(rs.getLong("target_account_id"));
        transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
        transaction.setDescription(rs.getString("description"));
        transaction.setReferenceNumber(rs.getString("reference_number"));
        transaction.setCreatedBy(rs.getLong("created_by"));
        transaction.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());

        transaction.setAccountNumber(rs.getString("account_number"));
        transaction.setTargetAccountNumber(rs.getString("target_account_number"));
        transaction.setCreatedByName(rs.getString("created_by_name"));

        return transaction;
    }

    public String generateReferenceNumber() {
        // Format: TXN + yyyyMMddHHmmssSSS + random 3 digits
        LocalDateTime now = LocalDateTime.now();
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(now);
        int random = new Random().nextInt(900) + 100; // 100-999
        return "TXN" + timestamp + random;
    }
}
