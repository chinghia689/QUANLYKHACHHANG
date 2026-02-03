package com.customer.model.dto;

import com.customer.model.Loan;
import com.customer.model.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoanReportData {
    private LocalDate fromDate;
    private LocalDate toDate;
    private LoanStatus filterStatus;

    private List<Loan> loans = new ArrayList<>();
    private int totalLoans;
    private BigDecimal totalOutstanding = BigDecimal.ZERO;

    private Map<LoanStatus, Integer> statusDistribution = new HashMap<>();
    private Map<LoanStatus, BigDecimal> statusAmounts = new HashMap<>();

    // Getters and Setters
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public LoanStatus getFilterStatus() { return filterStatus; }
    public void setFilterStatus(LoanStatus filterStatus) { this.filterStatus = filterStatus; }

    public List<Loan> getLoans() { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }

    public int getTotalLoans() { return totalLoans; }
    public void setTotalLoans(int totalLoans) { this.totalLoans = totalLoans; }

    public BigDecimal getTotalOutstanding() { return totalOutstanding; }
    public void setTotalOutstanding(BigDecimal totalOutstanding) { this.totalOutstanding = totalOutstanding; }

    public Map<LoanStatus, Integer> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<LoanStatus, Integer> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<LoanStatus, BigDecimal> getStatusAmounts() { return statusAmounts; }
    public void setStatusAmounts(Map<LoanStatus, BigDecimal> statusAmounts) { this.statusAmounts = statusAmounts; }
}
