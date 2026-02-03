package com.customer.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {
    private final LongProperty id;
    private final LongProperty customerId;
    private final LongProperty loanAccountId;
    private final StringProperty loanNumber;
    private final ObjectProperty<BigDecimal> principalAmount;
    private final ObjectProperty<BigDecimal> interestRate;
    private final IntegerProperty termMonths;
    private final ObjectProperty<BigDecimal> monthlyPayment;
    private final ObjectProperty<BigDecimal> totalPaid;
    private final ObjectProperty<BigDecimal> remainingBalance;
    private final ObjectProperty<LoanStatus> status;
    private final StringProperty purpose;
    private final ObjectProperty<LocalDateTime> appliedDate;
    private final ObjectProperty<LocalDateTime> approvedDate;
    private final LongProperty approvedBy;
    private final StringProperty approvalNote;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;
    private final LongProperty createdBy;
    private final ObjectProperty<LocalDateTime> createdDate;

    // Transient fields (không lưu DB)
    private final StringProperty customerName;
    private final StringProperty approverName;
    private final StringProperty creatorName;

    public Loan() {
        this.id = new SimpleLongProperty();
        this.customerId = new SimpleLongProperty();
        this.loanAccountId = new SimpleLongProperty();
        this.loanNumber = new SimpleStringProperty();
        this.principalAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.interestRate = new SimpleObjectProperty<>(new BigDecimal("12.00"));
        this.termMonths = new SimpleIntegerProperty();
        this.monthlyPayment = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.totalPaid = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.remainingBalance = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.status = new SimpleObjectProperty<>(LoanStatus.PENDING);
        this.purpose = new SimpleStringProperty();
        this.appliedDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.approvedDate = new SimpleObjectProperty<>();
        this.approvedBy = new SimpleLongProperty();
        this.approvalNote = new SimpleStringProperty();
        this.startDate = new SimpleObjectProperty<>();
        this.endDate = new SimpleObjectProperty<>();
        this.createdBy = new SimpleLongProperty();
        this.createdDate = new SimpleObjectProperty<>(LocalDateTime.now());

        this.customerName = new SimpleStringProperty();
        this.approverName = new SimpleStringProperty();
        this.creatorName = new SimpleStringProperty();
    }

    public Loan(long customerId, BigDecimal principalAmount, int termMonths, String purpose, long createdBy) {
        this();
        this.customerId.set(customerId);
        this.principalAmount.set(principalAmount);
        this.termMonths.set(termMonths);
        this.purpose.set(purpose);
        this.createdBy.set(createdBy);
    }

    // Calculate total amount to pay back
    public BigDecimal getTotalAmount() {
        if (monthlyPayment.get() != null && termMonths.get() > 0) {
            return monthlyPayment.get().multiply(new BigDecimal(termMonths.get()));
        }
        return BigDecimal.ZERO;
    }

    // Getters and Property methods

    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }

    public long getCustomerId() { return customerId.get(); }
    public void setCustomerId(long customerId) { this.customerId.set(customerId); }
    public LongProperty customerIdProperty() { return customerId; }

    public long getLoanAccountId() { return loanAccountId.get(); }
    public void setLoanAccountId(long loanAccountId) { this.loanAccountId.set(loanAccountId); }
    public LongProperty loanAccountIdProperty() { return loanAccountId; }

    public String getLoanNumber() { return loanNumber.get(); }
    public void setLoanNumber(String loanNumber) { this.loanNumber.set(loanNumber); }
    public StringProperty loanNumberProperty() { return loanNumber; }

    public BigDecimal getPrincipalAmount() { return principalAmount.get(); }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount.set(principalAmount); }
    public ObjectProperty<BigDecimal> principalAmountProperty() { return principalAmount; }

    public BigDecimal getInterestRate() { return interestRate.get(); }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate.set(interestRate); }
    public ObjectProperty<BigDecimal> interestRateProperty() { return interestRate; }

    public int getTermMonths() { return termMonths.get(); }
    public void setTermMonths(int termMonths) { this.termMonths.set(termMonths); }
    public IntegerProperty termMonthsProperty() { return termMonths; }

    public BigDecimal getMonthlyPayment() { return monthlyPayment.get(); }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment.set(monthlyPayment); }
    public ObjectProperty<BigDecimal> monthlyPaymentProperty() { return monthlyPayment; }

    public BigDecimal getTotalPaid() { return totalPaid.get(); }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid.set(totalPaid); }
    public ObjectProperty<BigDecimal> totalPaidProperty() { return totalPaid; }

    public BigDecimal getRemainingBalance() { return remainingBalance.get(); }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance.set(remainingBalance); }
    public ObjectProperty<BigDecimal> remainingBalanceProperty() { return remainingBalance; }

    public LoanStatus getStatus() { return status.get(); }
    public void setStatus(LoanStatus status) { this.status.set(status); }
    public ObjectProperty<LoanStatus> statusProperty() { return status; }

    public String getPurpose() { return purpose.get(); }
    public void setPurpose(String purpose) { this.purpose.set(purpose); }
    public StringProperty purposeProperty() { return purpose; }

    public LocalDateTime getAppliedDate() { return appliedDate.get(); }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate.set(appliedDate); }
    public ObjectProperty<LocalDateTime> appliedDateProperty() { return appliedDate; }

    public LocalDateTime getApprovedDate() { return approvedDate.get(); }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate.set(approvedDate); }
    public ObjectProperty<LocalDateTime> approvedDateProperty() { return approvedDate; }

    public long getApprovedBy() { return approvedBy.get(); }
    public void setApprovedBy(long approvedBy) { this.approvedBy.set(approvedBy); }
    public LongProperty approvedByProperty() { return approvedBy; }

    public String getApprovalNote() { return approvalNote.get(); }
    public void setApprovalNote(String approvalNote) { this.approvalNote.set(approvalNote); }
    public StringProperty approvalNoteProperty() { return approvalNote; }

    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate startDate) { this.startDate.set(startDate); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

    public LocalDate getEndDate() { return endDate.get(); }
    public void setEndDate(LocalDate endDate) { this.endDate.set(endDate); }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }

    public long getCreatedBy() { return createdBy.get(); }
    public void setCreatedBy(long createdBy) { this.createdBy.set(createdBy); }
    public LongProperty createdByProperty() { return createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate.set(createdDate); }
    public ObjectProperty<LocalDateTime> createdDateProperty() { return createdDate; }

    // Transient getters/setters
    public String getCustomerName() { return customerName.get(); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public StringProperty customerNameProperty() { return customerName; }

    public String getApproverName() { return approverName.get(); }
    public void setApproverName(String approverName) { this.approverName.set(approverName); }
    public StringProperty approverNameProperty() { return approverName; }

    public String getCreatorName() { return creatorName.get(); }
    public void setCreatorName(String creatorName) { this.creatorName.set(creatorName); }
    public StringProperty creatorNameProperty() { return creatorName; }
}
