package com.customer.service;

import com.customer.dao.AccountDAO;
import com.customer.model.Account;
import com.customer.model.AccountStatus;
import com.customer.model.AccountType;
import com.customer.model.Role;
import com.customer.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AccountService {
    private final AccountDAO accountDAO;

    // Interest Rates
    public static final double RATE_3_MONTHS = 4.00;
    public static final double RATE_6_MONTHS = 5.00;
    public static final double RATE_12_MONTHS = 6.00;
    public static final double RATE_DEFAULT = 0.50; // For Checking or < 3 months

    public AccountService() {
        this.accountDAO = new AccountDAO();
    }

    public List<Account> searchAccounts(String keyword, AccountType type, AccountStatus status) throws SQLException {
        return accountDAO.search(keyword, type, status);
    }

    public void openAccount(Account account) throws SQLException, ValidationException {
        // Permission check
        if (!canOpenAccount()) {
            throw new ValidationException("You do not have permission to open accounts.");
        }

        // Validate basic fields
        if (account.getCustomerId() <= 0) {
            throw new ValidationException("Please select a customer.");
        }
        if (account.getAccountType() == null) {
            throw new ValidationException("Please select an account type.");
        }

        // Check duplicate active account
        if (accountDAO.hasAccountOfType(account.getCustomerId(), account.getAccountType())) {
            throw new ValidationException("This customer already has an active " + account.getAccountType() + " account.");
        }

        // Generate account number
        account.setAccountNumber(accountDAO.generateAccountNumber());

        // Set defaults
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedDate(LocalDateTime.now());

        // Save
        accountDAO.save(account);
    }

    public void freezeAccount(Account account) throws SQLException, ValidationException {
        if (!canFreezeAccount()) {
            throw new ValidationException("You do not have permission to freeze accounts.");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only active accounts can be frozen.");
        }

        accountDAO.updateStatus(account.getId(), AccountStatus.FROZEN);
    }

    public void unfreezeAccount(Account account) throws SQLException, ValidationException {
        if (!canUnfreezeAccount()) {
            throw new ValidationException("Only Managers and Admins can unfreeze accounts.");
        }

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new ValidationException("Only frozen accounts can be unfrozen.");
        }

        accountDAO.updateStatus(account.getId(), AccountStatus.ACTIVE);
    }

    public void closeAccount(Account account) throws SQLException, ValidationException {
        if (!canCloseAccount()) {
            throw new ValidationException("Only Managers and Admins can close accounts.");
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Account is already closed.");
        }

        // Check balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Account balance must be 0 to close.");
        }

        accountDAO.updateStatus(account.getId(), AccountStatus.CLOSED);
    }

    public double getInterestRateByTerm(int months) {
        if (months >= 12) return RATE_12_MONTHS;
        if (months >= 6) return RATE_6_MONTHS;
        if (months >= 3) return RATE_3_MONTHS;
        return RATE_DEFAULT;
    }

    // Permissions
    public boolean canOpenAccount() {
        return SessionManager.isLoggedIn(); // All staff can open
    }

    public boolean canFreezeAccount() {
        return SessionManager.isLoggedIn(); // All staff can freeze
    }

    public boolean canUnfreezeAccount() {
        return SessionManager.hasRole(Role.MANAGER, Role.ADMIN);
    }

    public boolean canCloseAccount() {
        return SessionManager.hasRole(Role.MANAGER, Role.ADMIN);
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}
