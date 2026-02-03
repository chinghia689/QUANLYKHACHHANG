package com.customer.dao;

import com.customer.model.*;
import com.customer.model.dto.DashboardStats;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {
    private final Connection connection;

    public ReportDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // --- Dashboard Aggregations ---

    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public BigDecimal getTotalBalance() throws SQLException {
        String sql = "SELECT SUM(balance) FROM accounts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalLoans() throws SQLException {
        String sql = "SELECT SUM(principal_amount) FROM loans WHERE status = 'DISBURSED' OR status = 'OVERDUE'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    public int getTodayTransactionCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE DATE(created_date) = CURDATE()";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getCustomersInMonth(int year, int month) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE YEAR(created_date) <= ? AND MONTH(created_date) <= ? AND (YEAR(created_date) < ? OR MONTH(created_date) <= ?)";
        // Logic check: We want total customers up to end of previous month.
        // Simplified: Count customers created before end of specific month.
        // Actually, for "comparison", we usually want total customers AT THE END of previous month.

        String query = "SELECT COUNT(*) FROM customers WHERE created_date < ?";
        LocalDateTime endOfMonth = LocalDateTime.of(year, month, 1, 0, 0).plusMonths(1);

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(endOfMonth));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // Calculate total balance at end of specific month (Approximate based on current balance - transactions since then? No, that's too hard)
    // For simplicity, we might just track "created accounts balance" or similar, but accurate historical balance requires transaction replay.
    // Plan B for Balance Comparison: Just use current balance vs. (Current Balance - Net Transactions this Month)
    public BigDecimal getBalanceInMonth(int year, int month) throws SQLException {
        // This is complex. Let's simplify:
        // Current Balance = X
        // Transactions since Start of Month: In/Out
        // Prev Month Balance = Current Balance - (Deposits - Withdrawals - Transfers_Out + Transfers_In) since Start of Month

        // Let's implement this logic in Service or specialized query here?
        // Let's stick to simple "Snapshot" logic if we had history tables, but we don't.
        // So we reverse calculate from current balance.

        // 1. Get current total balance
        BigDecimal currentTotal = getTotalBalance();

        // 2. Get net change since start of current month
        LocalDate startOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        if (year < LocalDate.now().getYear() || (year == LocalDate.now().getYear() && month < LocalDate.now().getMonthValue())) {
             // Requesting past month not current.
             // This method might be better named getBalanceAtEndOfMonth(year, month)
             // But let's assume we want "Previous Month Balance" relative to NOW.
             // So if today is Feb, we want Jan 31st balance.

             // If year/month is exactly previous month:
             LocalDateTime cutoff = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

             // Sum transactions >= cutoff
             String sql = "SELECT " +
                     "SUM(CASE WHEN transaction_type = 'DEPOSIT' THEN amount " +
                     "         WHEN transaction_type = 'WITHDRAW' THEN -amount " +
                     "         WHEN transaction_type = 'LOAN_DISBURSEMENT' THEN amount " +
                     "         WHEN transaction_type = 'LOAN_PAYMENT' THEN -amount " +
                     "         WHEN transaction_type = 'TRANSFER' THEN 0 " + // Net 0 for internal
                     "         ELSE 0 END) " +
                     "FROM transactions WHERE created_date >= ?";

             // Wait, TRANSFER creates 2 transactions? Or 1?
             // Looking at TransactionDAO/Service (need to check), usually Transfer creates 2 records or 1 record with target?
             // Transaction model has targetAccountId.
             // If 1 record: source decreases, target increases. Net change to system = 0.
             // So Transfers don't affect TOTAL system balance.
             // Only Deposits (+) and Withdrawals (-).

             try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                 pstmt.setTimestamp(1, Timestamp.valueOf(cutoff));
                 try (ResultSet rs = pstmt.executeQuery()) {
                     if (rs.next()) {
                         BigDecimal netChange = rs.getBigDecimal(1);
                         if (netChange != null) {
                             return currentTotal.subtract(netChange);
                         }
                     }
                 }
             }
             return currentTotal;
        }
        return currentTotal; // Default fallback
    }

    // --- Top Rankings ---

    public List<Account> getTopAccountsByBalance(int limit) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name as customer_name " +
                     "FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.id " +
                     "ORDER BY a.balance DESC LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(extractAccountFromResultSet(rs));
                }
            }
        }
        return accounts;
    }

    public List<Account> getTopActiveAccounts(int limit) throws SQLException {
        // "Active" defined by number of transactions? Or just status?
        // Let's assume most transactions.
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name as customer_name, COUNT(t.id) as txn_count " +
                     "FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.id " +
                     "JOIN transactions t ON a.id = t.account_id " +
                     "GROUP BY a.id " +
                     "ORDER BY txn_count DESC LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(extractAccountFromResultSet(rs));
                }
            }
        }
        return accounts;
    }

    // --- Chart Data ---

    public Map<CustomerType, Integer> getCustomerDistribution() throws SQLException {
        Map<CustomerType, Integer> map = new HashMap<>();
        String sql = "SELECT customer_type, COUNT(*) FROM customers GROUP BY customer_type";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    CustomerType type = CustomerType.valueOf(rs.getString(1));
                    map.put(type, rs.getInt(2));
                } catch (IllegalArgumentException e) {
                    // Ignore unknown types
                }
            }
        }
        return map;
    }

    public Map<AccountType, Integer> getAccountTypeDistribution() throws SQLException {
        Map<AccountType, Integer> map = new HashMap<>();
        String sql = "SELECT account_type, COUNT(*) FROM accounts GROUP BY account_type";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    AccountType type = AccountType.valueOf(rs.getString(1));
                    map.put(type, rs.getInt(2));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
        }
        return map;
    }

    public Map<LoanStatus, Integer> getLoanStatusDistribution() throws SQLException {
        Map<LoanStatus, Integer> map = new HashMap<>();
        String sql = "SELECT status, COUNT(*) FROM loans GROUP BY status";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    LoanStatus status = LoanStatus.valueOf(rs.getString(1));
                    map.put(status, rs.getInt(2));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
        }
        return map;
    }

    public Map<String, Integer> getTransactionsByMonth(int months) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        // Last X months
        String sql = "SELECT DATE_FORMAT(created_date, '%Y-%m') as month, COUNT(*) " +
                     "FROM transactions " +
                     "WHERE created_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                     "GROUP BY DATE_FORMAT(created_date, '%Y-%m') " +
                     "ORDER BY month ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, months);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt(2));
                }
            }
        }
        return map;
    }

    public Map<String, BigDecimal> getBalanceTrendByMonth(int months) throws SQLException {
        // This is tricky without history table.
        // We will approximate by: Sum of all deposits - withdrawals for each month
        // This represents "Net Flow" rather than "Total Balance".
        // For a Trend Line, Net Flow is useful, or we can accumulate it.
        // Let's return Net Flow per month.

        Map<String, BigDecimal> map = new HashMap<>();
        String sql = "SELECT DATE_FORMAT(created_date, '%Y-%m') as month, " +
                     "SUM(CASE WHEN transaction_type = 'DEPOSIT' THEN amount " +
                     "         WHEN transaction_type = 'WITHDRAW' THEN -amount " +
                     "         WHEN transaction_type = 'LOAN_DISBURSEMENT' THEN amount " +
                     "         WHEN transaction_type = 'LOAN_PAYMENT' THEN -amount " +
                     "         ELSE 0 END) " +
                     "FROM transactions " +
                     "WHERE created_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                     "GROUP BY DATE_FORMAT(created_date, '%Y-%m') " +
                     "ORDER BY month ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, months);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal val = rs.getBigDecimal(2);
                    map.put(rs.getString(1), val != null ? val : BigDecimal.ZERO);
                }
            }
        }
        return map;
    }

    // --- Reports ---

    public List<Transaction> getTransactionsByFilter(LocalDate from, LocalDate to, TransactionType type, Long accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, a.account_number, ta.account_number as target_account_number, u.full_name as created_by_name " +
                "FROM transactions t " +
                "LEFT JOIN accounts a ON t.account_id = a.id " +
                "LEFT JOIN accounts ta ON t.target_account_id = ta.id " +
                "LEFT JOIN users u ON t.created_by = u.id " +
                "WHERE t.created_date BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(from.atStartOfDay()));
        params.add(Timestamp.valueOf(to.atTime(LocalTime.MAX)));

        if (type != null) {
            sql.append("AND t.transaction_type = ? ");
            params.add(type.name());
        }

        if (accountId != null) {
            sql.append("AND (t.account_id = ? OR t.target_account_id = ?) ");
            params.add(accountId);
            params.add(accountId);
        }

        sql.append("ORDER BY t.created_date DESC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    public List<Loan> getLoansByFilter(LocalDate from, LocalDate to, LoanStatus status) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT l.*, c.full_name as customer_name " +
                "FROM loans l " +
                "JOIN customers c ON l.customer_id = c.id " +
                "WHERE l.created_date BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(from.atStartOfDay()));
        params.add(Timestamp.valueOf(to.atTime(LocalTime.MAX)));

        if (status != null) {
            sql.append("AND l.status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY l.created_date DESC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
        }
        return loans;
    }

    public BigDecimal getOpeningBalance(long accountId, LocalDate date) throws SQLException {
        // Opening balance at start of date = Current Balance - Net transactions since date
        // Wait, "since date" means from date to NOW.
        // Opening Balance = Current Balance - (Sum of (Deposits - Withdrawals) from date to NOW)
        // Note: We need to be careful with signs.
        // Current = Opening + NetChange
        // Opening = Current - NetChange

        // First get current balance
        BigDecimal currentBalance = BigDecimal.ZERO;
        String balSql = "SELECT balance FROM accounts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(balSql)) {
            pstmt.setLong(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentBalance = rs.getBigDecimal(1);
                }
            }
        }

        // Calculate net change from date to NOW
        String txnSql = "SELECT " +
                "SUM(CASE " +
                "  WHEN account_id = ? AND transaction_type = 'DEPOSIT' THEN amount " +
                "  WHEN account_id = ? AND transaction_type = 'WITHDRAW' THEN -amount " +
                "  WHEN account_id = ? AND transaction_type = 'TRANSFER' THEN -amount " +
                "  WHEN target_account_id = ? AND transaction_type = 'TRANSFER' THEN amount " +
                "  WHEN account_id = ? AND transaction_type = 'LOAN_DISBURSEMENT' THEN amount " +
                "  WHEN account_id = ? AND transaction_type = 'LOAN_PAYMENT' THEN -amount " +
                "  ELSE 0 END) " +
                "FROM transactions " +
                "WHERE created_date >= ?";

        // NOTE: For Transfer, Source is Debit (-), Target is Credit (+)

        try (PreparedStatement pstmt = connection.prepareStatement(txnSql)) {
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, accountId);
            pstmt.setLong(3, accountId);
            pstmt.setLong(4, accountId);
            pstmt.setLong(5, accountId);
            pstmt.setLong(6, accountId);
            pstmt.setTimestamp(7, Timestamp.valueOf(date.atStartOfDay()));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal netChange = rs.getBigDecimal(1);
                    if (netChange != null) {
                        return currentBalance.subtract(netChange);
                    }
                }
            }
        }

        return currentBalance;
    }

    // --- Helpers ---

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

        // Handle customer_name if present
        try {
            String name = rs.getString("customer_name");
            if (name != null) account.setCustomerName(name);
        } catch (SQLException e) { /* ignore */ }

        return account;
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

        try {
            transaction.setAccountNumber(rs.getString("account_number"));
            transaction.setTargetAccountNumber(rs.getString("target_account_number"));
            transaction.setCreatedByName(rs.getString("created_by_name"));
        } catch (SQLException e) { /* ignore */ }

        return transaction;
    }

    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getLong("id"));
        loan.setCustomerId(rs.getLong("customer_id"));
        loan.setPrincipalAmount(rs.getBigDecimal("principal_amount"));
        loan.setInterestRate(BigDecimal.valueOf(rs.getDouble("interest_rate")));
        loan.setTermMonths(rs.getInt("term_months"));
        loan.setStartDate(rs.getDate("start_date").toLocalDate());
        if (rs.getDate("end_date") != null) {
            loan.setEndDate(rs.getDate("end_date").toLocalDate());
        }
        loan.setStatus(LoanStatus.valueOf(rs.getString("status")));
        loan.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());

        try {
            loan.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException e) { /* ignore */ }

        return loan;
    }
}
