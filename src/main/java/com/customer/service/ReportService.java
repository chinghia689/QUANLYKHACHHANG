package com.customer.service;

import com.customer.dao.ReportDAO;
import com.customer.model.Account;
import com.customer.model.LoanStatus;
import com.customer.model.Role;
import com.customer.model.TransactionType;
import com.customer.model.dto.AccountStatementData;
import com.customer.model.dto.DashboardStats;
import com.customer.model.dto.LoanReportData;
import com.customer.model.dto.TransactionReportData;
import com.customer.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        // Load current totals
        stats.setTotalCustomers(reportDAO.getTotalCustomers());
        stats.setTotalBalance(reportDAO.getTotalBalance());
        stats.setTotalLoans(reportDAO.getTotalLoans());
        stats.setTodayTransactions(reportDAO.getTodayTransactionCount());

        // Load previous month comparisons
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int prevMonth = currentMonth - 1;
        int prevYear = currentYear;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear--;
        }

        stats.setPreviousMonthCustomers(reportDAO.getCustomersInMonth(prevYear, prevMonth));
        stats.setPreviousMonthBalance(reportDAO.getBalanceInMonth(prevYear, prevMonth));

        // Load distributions
        stats.setCustomerDistribution(reportDAO.getCustomerDistribution());
        stats.setAccountTypeDistribution(reportDAO.getAccountTypeDistribution());
        stats.setLoanStatusDistribution(reportDAO.getLoanStatusDistribution());

        // Load trends (6 months)
        stats.setTransactionTrend(reportDAO.getTransactionsByMonth(6));
        stats.setBalanceTrend(reportDAO.getBalanceTrendByMonth(6));

        // Load top lists
        stats.setTopAccounts(reportDAO.getTopAccountsByBalance(5));
        stats.setTopActiveAccounts(reportDAO.getTopActiveAccounts(5));

        return stats;
    }

    public TransactionReportData getTransactionReport(LocalDate from, LocalDate to, TransactionType type, Long accountId) throws SQLException {
        TransactionReportData data = new TransactionReportData();
        data.setFromDate(from);
        data.setToDate(to);
        data.setFilterType(type);
        data.setAccountNumber(accountId);

        data.setTransactions(reportDAO.getTransactionsByFilter(from, to, type, accountId));

        // Calculate summaries
        int count = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;
        BigDecimal depTotal = BigDecimal.ZERO;
        BigDecimal withTotal = BigDecimal.ZERO;
        BigDecimal transTotal = BigDecimal.ZERO;

        for (var txn : data.getTransactions()) {
            count++;
            totalAmt = totalAmt.add(txn.getAmount());

            switch (txn.getTransactionType()) {
                case DEPOSIT:
                    depTotal = depTotal.add(txn.getAmount());
                    break;
                case WITHDRAW:
                    withTotal = withTotal.add(txn.getAmount());
                    break;
                case TRANSFER:
                    transTotal = transTotal.add(txn.getAmount());
                    break;
                case LOAN_DISBURSEMENT: // Treated like deposit for the account
                    depTotal = depTotal.add(txn.getAmount());
                    break;
                case LOAN_PAYMENT: // Treated like withdrawal
                    withTotal = withTotal.add(txn.getAmount());
                    break;
            }
        }

        data.setTotalCount(count);
        data.setTotalAmount(totalAmt);
        data.setDepositTotal(depTotal);
        data.setWithdrawTotal(withTotal);
        data.setTransferTotal(transTotal);

        return data;
    }

    public LoanReportData getLoanReport(LocalDate from, LocalDate to, LoanStatus status) throws SQLException {
        LoanReportData data = new LoanReportData();
        data.setFromDate(from);
        data.setToDate(to);
        data.setFilterStatus(status);

        data.setLoans(reportDAO.getLoansByFilter(from, to, status));

        // Summaries
        int count = 0;
        BigDecimal outstanding = BigDecimal.ZERO;

        for (var loan : data.getLoans()) {
            count++;
            if (loan.getStatus() == LoanStatus.DISBURSED || loan.getStatus() == LoanStatus.OVERDUE) {
                outstanding = outstanding.add(loan.getPrincipalAmount());
            }
        }

        data.setTotalLoans(count);
        data.setTotalOutstanding(outstanding);

        // Populate distributions for the chart in this report context if needed
        // Ideally we filter the distribution by date range too, but DAO `getLoanStatusDistribution` is global.
        // For report specificity, we can calculate from the list.
        for (var loan : data.getLoans()) {
            data.getStatusDistribution().merge(loan.getStatus(), 1, Integer::sum);
            data.getStatusAmounts().merge(loan.getStatus(), loan.getPrincipalAmount(), BigDecimal::add);
        }

        return data;
    }

    public AccountStatementData getAccountStatement(Account account, LocalDate from, LocalDate to) throws SQLException {
        AccountStatementData data = new AccountStatementData();
        data.setAccount(account);
        data.setFromDate(from);
        data.setToDate(to);
        data.setGeneratedBy(SessionManager.getCurrentUser().getFullName());
        data.setGeneratedAt(LocalDateTime.now());

        // Calculate opening balance
        BigDecimal opening = reportDAO.getOpeningBalance(account.getId(), from);
        data.setOpeningBalance(opening);

        // Get transactions
        data.setTransactions(reportDAO.getTransactionsByFilter(from, to, null, account.getId()));

        // Calculate closing and totals
        BigDecimal runningBalance = opening;
        BigDecimal dep = BigDecimal.ZERO;
        BigDecimal with = BigDecimal.ZERO;
        BigDecimal trans = BigDecimal.ZERO;

        for (var txn : data.getTransactions()) {
            // Note: transaction list is DESC date order.
            // For running balance calculation we usually want ASC.
            // But here we just want totals.

            // Wait, we need to verify if the transaction list is for THIS account as Source or Target?
            // `getTransactionsByFilter` returns where account_id OR target_account_id matches.

            boolean isIncoming = false;
            if (txn.getTransactionType() == TransactionType.DEPOSIT || txn.getTransactionType() == TransactionType.LOAN_DISBURSEMENT) {
                isIncoming = true;
            } else if (txn.getTransactionType() == TransactionType.TRANSFER) {
                 if (txn.getTargetAccountId() == account.getId()) {
                     isIncoming = true;
                 }
            }

            if (isIncoming) {
                dep = dep.add(txn.getAmount());
                // If it's transfer incoming, we might want to count it as Deposit or Transfer?
                if (txn.getTransactionType() == TransactionType.TRANSFER) trans = trans.add(txn.getAmount());
            } else {
                // Outgoing
                with = with.add(txn.getAmount());
                 if (txn.getTransactionType() == TransactionType.TRANSFER) trans = trans.add(txn.getAmount());
            }
        }

        // Closing balance = Opening + Incoming - Outgoing
        // Actually, we can just use `opening` and apply the net change.
        // Or simpler: Get closing balance as of `to` date?
        // Since we have the list, let's trust the totals.
        // Wait, Transfer count in `trans` is sum of absolute values.

        // Refined Totals:
        BigDecimal totalCredits = BigDecimal.ZERO; // Adds to balance
        BigDecimal totalDebits = BigDecimal.ZERO;  // Subtracts from balance

        for (var txn : data.getTransactions()) {
            boolean isCredit = false;
             if (txn.getTransactionType() == TransactionType.DEPOSIT || txn.getTransactionType() == TransactionType.LOAN_DISBURSEMENT) {
                isCredit = true;
            } else if (txn.getTransactionType() == TransactionType.TRANSFER && txn.getTargetAccountId() == account.getId()) {
                isCredit = true;
            }

            if (isCredit) {
                totalCredits = totalCredits.add(txn.getAmount());
            } else {
                totalDebits = totalDebits.add(txn.getAmount());
            }
        }

        data.setTotalDeposits(totalCredits);
        data.setTotalWithdrawals(totalDebits);

        data.setClosingBalance(opening.add(totalCredits).subtract(totalDebits));

        return data;
    }

    public boolean canExportPDF() {
        return SessionManager.hasRole(Role.MANAGER, Role.ADMIN);
    }
}
