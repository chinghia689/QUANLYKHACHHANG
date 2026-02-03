package com.customer.model.dto;

import com.customer.model.Transaction;
import com.customer.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionReportData {
    private LocalDate fromDate;
    private LocalDate toDate;
    private TransactionType filterType;
    private Long accountNumber;

    private List<Transaction> transactions = new ArrayList<>();
    private int totalCount;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private BigDecimal depositTotal = BigDecimal.ZERO;
    private BigDecimal withdrawTotal = BigDecimal.ZERO;
    private BigDecimal transferTotal = BigDecimal.ZERO;

    // Getters and Setters
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public TransactionType getFilterType() { return filterType; }
    public void setFilterType(TransactionType filterType) { this.filterType = filterType; }

    public Long getAccountNumber() { return accountNumber; }
    public void setAccountNumber(Long accountNumber) { this.accountNumber = accountNumber; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositTotal() { return depositTotal; }
    public void setDepositTotal(BigDecimal depositTotal) { this.depositTotal = depositTotal; }

    public BigDecimal getWithdrawTotal() { return withdrawTotal; }
    public void setWithdrawTotal(BigDecimal withdrawTotal) { this.withdrawTotal = withdrawTotal; }

    public BigDecimal getTransferTotal() { return transferTotal; }
    public void setTransferTotal(BigDecimal transferTotal) { this.transferTotal = transferTotal; }
}
