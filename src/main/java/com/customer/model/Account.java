package com.customer.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private final LongProperty id;
    private final LongProperty customerId;
    private final StringProperty accountNumber;
    private final ObjectProperty<AccountType> accountType;
    private final ObjectProperty<BigDecimal> balance;
    private final DoubleProperty interestRate;
    private final IntegerProperty termMonths;
    private final ObjectProperty<AccountStatus> status;
    private final ObjectProperty<LocalDateTime> createdDate;
    private final ObjectProperty<LocalDateTime> closedDate;

    // Transient field for display
    private final StringProperty customerName;

    public Account() {
        this(null, 0, "", AccountType.CHECKING, BigDecimal.ZERO, 0.0, 0, AccountStatus.ACTIVE, LocalDateTime.now(), null, "");
    }

    public Account(Long id, long customerId, String accountNumber, AccountType accountType,
                  BigDecimal balance, double interestRate, int termMonths,
                  AccountStatus status, LocalDateTime createdDate, LocalDateTime closedDate, String customerName) {
        this.id = new SimpleLongProperty(id != null ? id : 0);
        this.customerId = new SimpleLongProperty(customerId);
        this.accountNumber = new SimpleStringProperty(accountNumber);
        this.accountType = new SimpleObjectProperty<>(accountType);
        this.balance = new SimpleObjectProperty<>(balance);
        this.interestRate = new SimpleDoubleProperty(interestRate);
        this.termMonths = new SimpleIntegerProperty(termMonths);
        this.status = new SimpleObjectProperty<>(status);
        this.createdDate = new SimpleObjectProperty<>(createdDate);
        this.closedDate = new SimpleObjectProperty<>(closedDate);
        this.customerName = new SimpleStringProperty(customerName);
    }

    // ID Property
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }

    // Customer ID Property
    public long getCustomerId() { return customerId.get(); }
    public void setCustomerId(long customerId) { this.customerId.set(customerId); }
    public LongProperty customerIdProperty() { return customerId; }

    // Account Number Property
    public String getAccountNumber() { return accountNumber.get(); }
    public void setAccountNumber(String accountNumber) { this.accountNumber.set(accountNumber); }
    public StringProperty accountNumberProperty() { return accountNumber; }

    // Account Type Property
    public AccountType getAccountType() { return accountType.get(); }
    public void setAccountType(AccountType accountType) { this.accountType.set(accountType); }
    public ObjectProperty<AccountType> accountTypeProperty() { return accountType; }

    // Balance Property
    public BigDecimal getBalance() { return balance.get(); }
    public void setBalance(BigDecimal balance) { this.balance.set(balance); }
    public ObjectProperty<BigDecimal> balanceProperty() { return balance; }

    // Interest Rate Property
    public double getInterestRate() { return interestRate.get(); }
    public void setInterestRate(double interestRate) { this.interestRate.set(interestRate); }
    public DoubleProperty interestRateProperty() { return interestRate; }

    // Term Months Property
    public int getTermMonths() { return termMonths.get(); }
    public void setTermMonths(int termMonths) { this.termMonths.set(termMonths); }
    public IntegerProperty termMonthsProperty() { return termMonths; }

    // Status Property
    public AccountStatus getStatus() { return status.get(); }
    public void setStatus(AccountStatus status) { this.status.set(status); }
    public ObjectProperty<AccountStatus> statusProperty() { return status; }

    // Created Date Property
    public LocalDateTime getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate.set(createdDate); }
    public ObjectProperty<LocalDateTime> createdDateProperty() { return createdDate; }

    // Closed Date Property
    public LocalDateTime getClosedDate() { return closedDate.get(); }
    public void setClosedDate(LocalDateTime closedDate) { this.closedDate.set(closedDate); }
    public ObjectProperty<LocalDateTime> closedDateProperty() { return closedDate; }

    // Customer Name Property (Transient)
    public String getCustomerName() { return customerName.get(); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public StringProperty customerNameProperty() { return customerName; }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + getId() +
                ", accountNumber='" + getAccountNumber() + '\'' +
                ", accountType=" + getAccountType() +
                ", balance=" + getBalance() +
                ", status=" + getStatus() +
                '}';
    }
}
