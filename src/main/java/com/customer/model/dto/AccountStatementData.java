package com.customer.model.dto;

import com.customer.model.Account;
import com.customer.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountStatementData {
    private Account account;
    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;

    private List<Transaction> transactions = new ArrayList<>();

    private BigDecimal totalDeposits = BigDecimal.ZERO;
    private BigDecimal totalWithdrawals = BigDecimal.ZERO;
    private BigDecimal totalTransfers = BigDecimal.ZERO;

    private String generatedBy;
    private LocalDateTime generatedAt;

    // Getters and Setters
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(BigDecimal totalDeposits) { this.totalDeposits = totalDeposits; }

    public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(BigDecimal totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }

    public BigDecimal getTotalTransfers() { return totalTransfers; }
    public void setTotalTransfers(BigDecimal totalTransfers) { this.totalTransfers = totalTransfers; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
