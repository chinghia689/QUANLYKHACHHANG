package com.customer.service;

import com.customer.dao.AccountDAO;
import com.customer.dao.DatabaseManager;
import com.customer.dao.TransactionDAO;
import com.customer.model.Account;
import com.customer.model.AccountStatus;
import com.customer.model.Transaction;
import com.customer.model.TransactionType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    public static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("500000000"); // 500 million VND

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
    }

    public Transaction deposit(long accountId, BigDecimal amount, String description, long userId) throws SQLException, IllegalArgumentException {
        validateAmount(amount);

        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại");
        }
        validateAccountStatus(account);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(description);
        transaction.setCreatedBy(userId);

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Update account balance
            accountDAO.update(account, conn); // Requires update method in AccountDAO that accepts connection

            // Save transaction
            transactionDAO.save(transaction, conn);

            conn.commit();
            return transaction;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Do NOT close connection here as it is a shared singleton connection
                    // conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Transaction withdraw(long accountId, BigDecimal amount, String description, long userId) throws SQLException, IllegalArgumentException {
        validateAmount(amount);

        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại");
        }
        validateAccountStatus(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(description);
        transaction.setCreatedBy(userId);

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            accountDAO.update(account, conn);
            transactionDAO.save(transaction, conn);

            conn.commit();
            return transaction;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Do NOT close connection here as it is a shared singleton connection
                    // conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Transaction transfer(long sourceAccountId, long targetAccountId, BigDecimal amount, String description, long userId) throws SQLException, IllegalArgumentException {
        validateAmount(amount);

        if (sourceAccountId == targetAccountId) {
            throw new IllegalArgumentException("Không thể chuyển tiền cho chính tài khoản nguồn");
        }

        Account sourceAccount = accountDAO.findById(sourceAccountId);
        if (sourceAccount == null) {
            throw new IllegalArgumentException("Tài khoản nguồn không tồn tại");
        }
        validateAccountStatus(sourceAccount);

        Account targetAccount = accountDAO.findById(targetAccountId);
        if (targetAccount == null) {
            throw new IllegalArgumentException("Tài khoản đích không tồn tại");
        }
        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài khoản đích không hoạt động");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư tài khoản nguồn không đủ");
        }

        // Calculate new balances
        BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal targetNewBalance = targetAccount.getBalance().add(amount);

        sourceAccount.setBalance(sourceNewBalance);
        targetAccount.setBalance(targetNewBalance);

        // Create transactions
        // 1. Debit from source
        Transaction sourceTx = new Transaction();
        sourceTx.setAccountId(sourceAccountId);
        sourceTx.setTransactionType(TransactionType.TRANSFER);
        sourceTx.setAmount(amount);
        sourceTx.setTargetAccountId(targetAccountId);
        sourceTx.setBalanceAfter(sourceNewBalance);
        sourceTx.setDescription("Chuyển tiền đến " + targetAccount.getAccountNumber() + ": " + description);
        sourceTx.setCreatedBy(userId);

        // 2. Credit to target (Optional: Create a receiving transaction record for target account)
        Transaction targetTx = new Transaction();
        targetTx.setAccountId(targetAccountId);
        targetTx.setTransactionType(TransactionType.TRANSFER); // Or DEPOSIT/TRANSFER_IN type if distinguished
        targetTx.setAmount(amount); // Positive for target? usually stored positive but context matters
        targetTx.setTargetAccountId(sourceAccountId);
        targetTx.setBalanceAfter(targetNewBalance);
        targetTx.setDescription("Nhận tiền từ " + sourceAccount.getAccountNumber() + ": " + description);
        targetTx.setCreatedBy(userId);

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Update balances
            accountDAO.update(sourceAccount, conn);
            accountDAO.update(targetAccount, conn);

            // Save transactions
            transactionDAO.save(sourceTx, conn);

            // Note: Saving transaction for target account is often good practice so they see it in their history
            // But requirements mentioned "Transaction Table" linking accounts.
            // If we want both to see history, we insert 2 records or query efficiently.
            // Plan says: "Lịch sử theo tài khoản", so better to insert record for target too.
            // However, Transaction table structure has target_account_id which links them.
            // If we insert 2 records, they should have different IDs but maybe link to same operation?
            // Simple approach: Insert 2 records so each account has a direct transaction entry.
            // Let's modify targetTx slightly to look like a Receive Transfer
            // Since we reused TransactionType.TRANSFER, we can differentiate by context or just keep it simple.
            // For now, inserting record for source is mandatory. Target record is optional but recommended.
            // Let's verify requirement: "Reference format TXN...".
            // If we insert 2 records, they will have different REF numbers unless we share it.
            // Let's generate one REF and share it? Or distinct REFs?
            // Standard banking: distinct transaction IDs, maybe same Ref ID or Linked ID.
            // Let's just save sourceTx for now as the primary record of the transfer action.
            // If we want the target to see it, we need to query where target_account_id = id OR account_id = id.
            // Let's check TransactionDAO.findByAccountId: "WHERE t.account_id = ?"
            // So if we only save sourceTx, target won't see it in standard query unless we change query.
            // CHANGING APPROACH: Insert 2 records for full history on both sides.
            targetTx.setReferenceNumber(transactionDAO.generateReferenceNumber()); // Distinct ref
            transactionDAO.save(targetTx, conn);

            conn.commit();
            return sourceTx;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    // Do NOT close connection here as it is a shared singleton connection
                    // conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Transaction> getTransactionHistory(long accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }

    public List<Transaction> searchTransactions(long accountId, LocalDateTime from, LocalDateTime to) throws SQLException {
        return transactionDAO.findByDateRange(accountId, from, to);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new IllegalArgumentException("Số tiền giao dịch vượt quá hạn mức (500,000,000 VND)");
        }
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài khoản đang không hoạt động (Trạng thái: " + account.getStatus() + ")");
        }
    }
}
