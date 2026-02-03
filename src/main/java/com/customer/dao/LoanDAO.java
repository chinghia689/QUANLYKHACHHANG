package com.customer.dao;

import com.customer.model.Loan;
import com.customer.model.LoanStatus;
import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    private final Connection connection;

    public LoanDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public String generateLoanNumber() throws SQLException {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "LN" + year;
        String sql = "SELECT MAX(loan_number) FROM loans WHERE loan_number LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String maxLoanNumber = rs.getString(1);
                    if (maxLoanNumber != null) {
                        int sequence = Integer.parseInt(maxLoanNumber.substring(6));
                        return prefix + String.format("%06d", sequence + 1);
                    }
                }
            }
        }
        return prefix + "000001";
    }

    public void save(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (customer_id, loan_number, principal_amount, interest_rate, " +
                     "term_months, monthly_payment, total_paid, remaining_balance, status, purpose, " +
                     "created_by, created_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, loan.getCustomerId());
            stmt.setString(2, loan.getLoanNumber());
            stmt.setBigDecimal(3, loan.getPrincipalAmount());
            stmt.setBigDecimal(4, loan.getInterestRate());
            stmt.setInt(5, loan.getTermMonths());
            stmt.setBigDecimal(6, loan.getMonthlyPayment());
            stmt.setBigDecimal(7, loan.getTotalPaid());
            stmt.setBigDecimal(8, loan.getRemainingBalance());
            stmt.setString(9, loan.getStatus().name());
            stmt.setString(10, loan.getPurpose());
            stmt.setLong(11, loan.getCreatedBy());
            stmt.setTimestamp(12, Timestamp.valueOf(loan.getCreatedDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating loan failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    loan.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating loan failed, no ID obtained.");
                }
            }
        }
    }

    public void update(Loan loan) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE loans SET ");
        sql.append("status = ?, approved_date = ?, approved_by = ?, approval_note = ?, ");
        sql.append("start_date = ?, end_date = ?, loan_account_id = ?, ");
        sql.append("total_paid = ?, remaining_balance = ? ");
        sql.append("WHERE id = ?");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            stmt.setString(1, loan.getStatus().name());
            stmt.setTimestamp(2, loan.getApprovedDate() != null ? Timestamp.valueOf(loan.getApprovedDate()) : null);
            if (loan.getApprovedBy() > 0) {
                stmt.setLong(3, loan.getApprovedBy());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setString(4, loan.getApprovalNote());
            stmt.setDate(5, loan.getStartDate() != null ? Date.valueOf(loan.getStartDate()) : null);
            stmt.setDate(6, loan.getEndDate() != null ? Date.valueOf(loan.getEndDate()) : null);
            if (loan.getLoanAccountId() > 0) {
                stmt.setLong(7, loan.getLoanAccountId());
            } else {
                stmt.setNull(7, Types.BIGINT);
            }
            stmt.setBigDecimal(8, loan.getTotalPaid());
            stmt.setBigDecimal(9, loan.getRemainingBalance());
            stmt.setLong(10, loan.getId());

            stmt.executeUpdate();
        }
    }

    public Loan findById(long id) throws SQLException {
        String sql = "SELECT l.*, c.full_name as customer_name, " +
                     "u1.full_name as approver_name, u2.full_name as creator_name " +
                     "FROM loans l " +
                     "JOIN customers c ON l.customer_id = c.id " +
                     "LEFT JOIN users u1 ON l.approved_by = u1.id " +
                     "JOIN users u2 ON l.created_by = u2.id " +
                     "WHERE l.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractLoanFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Loan> findByCustomerId(long customerId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, c.full_name as customer_name, " +
                     "u1.full_name as approver_name, u2.full_name as creator_name " +
                     "FROM loans l " +
                     "JOIN customers c ON l.customer_id = c.id " +
                     "LEFT JOIN users u1 ON l.approved_by = u1.id " +
                     "JOIN users u2 ON l.created_by = u2.id " +
                     "WHERE l.customer_id = ? " +
                     "ORDER BY l.created_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(extractLoanFromResultSet(rs));
                }
            }
        }
        return loans;
    }

    public List<Loan> search(String keyword, LoanStatus status, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT l.*, c.full_name as customer_name, " +
            "u1.full_name as approver_name, u2.full_name as creator_name " +
            "FROM loans l " +
            "JOIN customers c ON l.customer_id = c.id " +
            "LEFT JOIN users u1 ON l.approved_by = u1.id " +
            "JOIN users u2 ON l.created_by = u2.id " +
            "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (l.loan_number LIKE ? OR c.full_name LIKE ?) ");
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null) {
            sql.append("AND l.status = ? ");
            params.add(status.name());
        }

        if (fromDate != null) {
            sql.append("AND DATE(l.applied_date) >= ? ");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append("AND DATE(l.applied_date) <= ? ");
            params.add(Date.valueOf(toDate));
        }

        sql.append("ORDER BY l.applied_date DESC");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(extractLoanFromResultSet(rs));
                }
            }
        }
        return loans;
    }

    public List<Loan> findByStatus(LoanStatus status) throws SQLException {
        return search(null, status, null, null);
    }

    public boolean hasActiveLoan(long customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE customer_id = ? AND status IN ('DISBURSED', 'OVERDUE')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Loan extractLoanFromResultSet(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getLong("id"));
        loan.setCustomerId(rs.getLong("customer_id"));

        long loanAccountId = rs.getLong("loan_account_id");
        if (!rs.wasNull()) {
            loan.setLoanAccountId(loanAccountId);
        }

        loan.setLoanNumber(rs.getString("loan_number"));
        loan.setPrincipalAmount(rs.getBigDecimal("principal_amount"));
        loan.setInterestRate(rs.getBigDecimal("interest_rate"));
        loan.setTermMonths(rs.getInt("term_months"));
        loan.setMonthlyPayment(rs.getBigDecimal("monthly_payment"));
        loan.setTotalPaid(rs.getBigDecimal("total_paid"));
        loan.setRemainingBalance(rs.getBigDecimal("remaining_balance"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            loan.setStatus(LoanStatus.valueOf(statusStr));
        }

        loan.setPurpose(rs.getString("purpose"));

        Timestamp appliedDate = rs.getTimestamp("applied_date");
        if (appliedDate != null) {
            loan.setAppliedDate(appliedDate.toLocalDateTime());
        }

        Timestamp approvedDate = rs.getTimestamp("approved_date");
        if (approvedDate != null) {
            loan.setApprovedDate(approvedDate.toLocalDateTime());
        }

        long approvedBy = rs.getLong("approved_by");
        if (!rs.wasNull()) {
            loan.setApprovedBy(approvedBy);
        }

        loan.setApprovalNote(rs.getString("approval_note"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            loan.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            loan.setEndDate(endDate.toLocalDate());
        }

        loan.setCreatedBy(rs.getLong("created_by"));

        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            loan.setCreatedDate(createdDate.toLocalDateTime());
        }

        // Transient fields
        loan.setCustomerName(rs.getString("customer_name"));
        loan.setApproverName(rs.getString("approver_name"));
        loan.setCreatorName(rs.getString("creator_name"));

        return loan;
    }
}
