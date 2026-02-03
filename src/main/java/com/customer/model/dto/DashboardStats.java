package com.customer.model.dto;

import com.customer.model.Account;
import com.customer.model.AccountType;
import com.customer.model.CustomerType;
import com.customer.model.LoanStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStats {
    // Current totals
    private int totalCustomers;
    private BigDecimal totalBalance;
    private BigDecimal totalLoans;
    private int todayTransactions;

    // Previous month totals for comparison
    private int previousMonthCustomers;
    private BigDecimal previousMonthBalance;

    // Distribution maps
    private Map<CustomerType, Integer> customerDistribution = new HashMap<>();
    private Map<AccountType, Integer> accountTypeDistribution = new HashMap<>();
    private Map<LoanStatus, Integer> loanStatusDistribution = new HashMap<>();

    // Trend maps (Month -> Value)
    private Map<String, Integer> transactionTrend = new HashMap<>();
    private Map<String, BigDecimal> balanceTrend = new HashMap<>();

    // Top lists
    private List<Account> topAccounts = new ArrayList<>();
    private List<Account> topActiveAccounts = new ArrayList<>();

    // Getters and Setters
    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public BigDecimal getTotalLoans() { return totalLoans; }
    public void setTotalLoans(BigDecimal totalLoans) { this.totalLoans = totalLoans; }

    public int getTodayTransactions() { return todayTransactions; }
    public void setTodayTransactions(int todayTransactions) { this.todayTransactions = todayTransactions; }

    public int getPreviousMonthCustomers() { return previousMonthCustomers; }
    public void setPreviousMonthCustomers(int previousMonthCustomers) { this.previousMonthCustomers = previousMonthCustomers; }

    public BigDecimal getPreviousMonthBalance() { return previousMonthBalance; }
    public void setPreviousMonthBalance(BigDecimal previousMonthBalance) { this.previousMonthBalance = previousMonthBalance; }

    public Map<CustomerType, Integer> getCustomerDistribution() { return customerDistribution; }
    public void setCustomerDistribution(Map<CustomerType, Integer> customerDistribution) { this.customerDistribution = customerDistribution; }

    public Map<AccountType, Integer> getAccountTypeDistribution() { return accountTypeDistribution; }
    public void setAccountTypeDistribution(Map<AccountType, Integer> accountTypeDistribution) { this.accountTypeDistribution = accountTypeDistribution; }

    public Map<LoanStatus, Integer> getLoanStatusDistribution() { return loanStatusDistribution; }
    public void setLoanStatusDistribution(Map<LoanStatus, Integer> loanStatusDistribution) { this.loanStatusDistribution = loanStatusDistribution; }

    public Map<String, Integer> getTransactionTrend() { return transactionTrend; }
    public void setTransactionTrend(Map<String, Integer> transactionTrend) { this.transactionTrend = transactionTrend; }

    public Map<String, BigDecimal> getBalanceTrend() { return balanceTrend; }
    public void setBalanceTrend(Map<String, BigDecimal> balanceTrend) { this.balanceTrend = balanceTrend; }

    public List<Account> getTopAccounts() { return topAccounts; }
    public void setTopAccounts(List<Account> topAccounts) { this.topAccounts = topAccounts; }

    public List<Account> getTopActiveAccounts() { return topActiveAccounts; }
    public void setTopActiveAccounts(List<Account> topActiveAccounts) { this.topActiveAccounts = topActiveAccounts; }

    // Calculated methods
    public double getCustomerChangePercent() {
        if (previousMonthCustomers == 0) {
            return totalCustomers > 0 ? 100.0 : 0.0;
        }
        return ((double)(totalCustomers - previousMonthCustomers) * 100.0) / previousMonthCustomers;
    }

    public double getBalanceChangePercent() {
        if (previousMonthBalance == null || previousMonthBalance.compareTo(BigDecimal.ZERO) == 0) {
            return totalBalance != null && totalBalance.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        if (totalBalance == null) return -100.0;

        BigDecimal diff = totalBalance.subtract(previousMonthBalance);
        return diff.multiply(new BigDecimal(100)).divide(previousMonthBalance, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
