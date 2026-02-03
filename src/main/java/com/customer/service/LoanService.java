package com.customer.service;

import com.customer.dao.AccountDAO;
import com.customer.dao.LoanDAO;
import com.customer.model.AccountStatus;
import com.customer.model.AccountType;
import com.customer.model.Loan;
import com.customer.model.LoanStatus;
import com.customer.model.Role;
import com.customer.util.SessionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanService {
    public static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("10000000");  // 10M
    public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000"); // 1B
    public static final int MIN_TERM_MONTHS = 6;
    public static final int MAX_TERM_MONTHS = 60;
    public static final BigDecimal INTEREST_RATE = new BigDecimal("12.00");  // 12% per year

    private final LoanDAO loanDAO;
    private final AccountDAO accountDAO;

    public LoanService() {
        this.loanDAO = new LoanDAO();
        this.accountDAO = new AccountDAO();
    }

    public Loan applyLoan(long customerId, BigDecimal amount, int termMonths, String purpose, long createdBy) throws SQLException, ValidationException {
        // Validation
        if (amount.compareTo(MIN_LOAN_AMOUNT) < 0 || amount.compareTo(MAX_LOAN_AMOUNT) > 0) {
            throw new ValidationException("Số tiền vay phải từ 10,000,000 đến 1,000,000,000 VND");
        }
        if (termMonths < MIN_TERM_MONTHS || termMonths > MAX_TERM_MONTHS) {
            throw new ValidationException("Kỳ hạn vay phải từ 6 đến 60 tháng");
        }

        Loan loan = new Loan(customerId, amount, termMonths, purpose, createdBy);
        loan.setInterestRate(INTEREST_RATE);

        // Calculate payment
        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, INTEREST_RATE, termMonths);
        loan.setMonthlyPayment(monthlyPayment);

        // Generate loan number
        String loanNumber = loanDAO.generateLoanNumber();
        loan.setLoanNumber(loanNumber);

        // Initial values
        loan.setStatus(LoanStatus.PENDING);
        loan.setRemainingBalance(amount);
        loan.setTotalPaid(BigDecimal.ZERO);
        loan.setCreatedDate(LocalDateTime.now());
        loan.setAppliedDate(LocalDateTime.now());

        loanDAO.save(loan);
        return loan;
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int months) {
        if (months <= 0) return BigDecimal.ZERO;
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);

        // PMT = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusRatePowerN = monthlyRate.add(BigDecimal.ONE).pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowerN);
        BigDecimal denominator = onePlusRatePowerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public void approveLoan(long loanId, long approvedBy, String note) throws SQLException, ValidationException {
        if (!canApproveLoan()) {
            throw new ValidationException("Bạn không có quyền duyệt khoản vay");
        }

        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new ValidationException("Khoản vay không tồn tại");
        }
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ValidationException("Chỉ có thể duyệt khoản vay đang ở trạng thái Chờ duyệt");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(approvedBy);
        loan.setApprovedDate(LocalDateTime.now());
        loan.setApprovalNote(note);

        loanDAO.update(loan);
    }

    public void rejectLoan(long loanId, long approvedBy, String reason) throws SQLException, ValidationException {
        if (!canApproveLoan()) {
            throw new ValidationException("Bạn không có quyền từ chối khoản vay");
        }

        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new ValidationException("Khoản vay không tồn tại");
        }
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ValidationException("Chỉ có thể từ chối khoản vay đang ở trạng thái Chờ duyệt");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setApprovedBy(approvedBy);
        loan.setApprovedDate(LocalDateTime.now());
        loan.setApprovalNote(reason);

        loanDAO.update(loan);
    }

    public List<Loan> searchLoans(String keyword, LoanStatus status, LocalDate from, LocalDate to) throws SQLException {
        return loanDAO.search(keyword, status, from, to);
    }

    public boolean canApproveLoan() {
        return SessionManager.hasRole(Role.MANAGER, Role.ADMIN);
    }

    public void validateForDisbursement(long customerId) throws SQLException, ValidationException {
        // Check active checking account
        boolean hasChecking = accountDAO.hasAccountOfType(customerId, AccountType.CHECKING);
        if (!hasChecking) {
            throw new ValidationException("Khách hàng cần có tài khoản thanh toán (Checking) để giải ngân");
        }

        // Check for existing active loans
        if (loanDAO.hasActiveLoan(customerId)) {
            throw new ValidationException("Khách hàng đang có khoản vay chưa tất toán");
        }
    }

    public List<AmortizationEntry> generateAmortizationSchedule(Loan loan) {
        List<AmortizationEntry> schedule = new ArrayList<>();

        BigDecimal monthlyRate = loan.getInterestRate()
            .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = loan.getPrincipalAmount();
        LocalDate currentDate = loan.getStartDate() != null
            ? loan.getStartDate()
            : LocalDate.now();

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = loan.getMonthlyPayment().subtract(interestPayment);

            // Adjust last payment
            if (i == loan.getTermMonths()) {
                principalPayment = remainingBalance;
                // Total might differ slightly due to rounding
            } else if (principalPayment.compareTo(remainingBalance) > 0) {
                principalPayment = remainingBalance;
            }

            remainingBalance = remainingBalance.subtract(principalPayment);
            LocalDate dueDate = currentDate.plusMonths(i);

            schedule.add(new AmortizationEntry(
                i,
                dueDate,
                principalPayment,
                interestPayment,
                principalPayment.add(interestPayment),
                remainingBalance.max(BigDecimal.ZERO)
            ));

            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) break;
        }

        return schedule;
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class AmortizationEntry {
        private final int paymentNumber;
        private final LocalDate dueDate;
        private final BigDecimal principalPortion;
        private final BigDecimal interestPortion;
        private final BigDecimal totalPayment;
        private final BigDecimal remainingBalance;

        public AmortizationEntry(int paymentNumber, LocalDate dueDate, BigDecimal principalPortion,
                               BigDecimal interestPortion, BigDecimal totalPayment, BigDecimal remainingBalance) {
            this.paymentNumber = paymentNumber;
            this.dueDate = dueDate;
            this.principalPortion = principalPortion;
            this.interestPortion = interestPortion;
            this.totalPayment = totalPayment;
            this.remainingBalance = remainingBalance;
        }

        public int getPaymentNumber() { return paymentNumber; }
        public LocalDate getDueDate() { return dueDate; }
        public BigDecimal getPrincipalPortion() { return principalPortion; }
        public BigDecimal getInterestPortion() { return interestPortion; }
        public BigDecimal getTotalPayment() { return totalPayment; }
        public BigDecimal getRemainingBalance() { return remainingBalance; }
    }
}
