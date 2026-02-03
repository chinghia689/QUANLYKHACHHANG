package com.customer.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty accountId = new SimpleLongProperty();
    private final ObjectProperty<TransactionType> transactionType = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    private final LongProperty targetAccountId = new SimpleLongProperty();
    private final ObjectProperty<BigDecimal> balanceAfter = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty referenceNumber = new SimpleStringProperty();
    private final LongProperty createdBy = new SimpleLongProperty();
    private final ObjectProperty<LocalDateTime> createdDate = new SimpleObjectProperty<>();

    // Transient fields for display
    private final StringProperty accountNumber = new SimpleStringProperty();
    private final StringProperty targetAccountNumber = new SimpleStringProperty();
    private final StringProperty createdByName = new SimpleStringProperty();

    public Transaction() {
    }

    public Transaction(Long accountId, TransactionType type, BigDecimal amount,
                      String description, Long createdBy) {
        setAccountId(accountId);
        setTransactionType(type);
        setAmount(amount);
        setDescription(description);
        setCreatedBy(createdBy);
        setCreatedDate(LocalDateTime.now());
    }

    // Getters and Setters for Properties

    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public long getAccountId() {
        return accountId.get();
    }

    public LongProperty accountIdProperty() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId.set(accountId);
    }

    public TransactionType getTransactionType() {
        return transactionType.get();
    }

    public ObjectProperty<TransactionType> transactionTypeProperty() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType.set(transactionType);
    }

    public BigDecimal getAmount() {
        return amount.get();
    }

    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount.set(amount);
    }

    public long getTargetAccountId() {
        return targetAccountId.get();
    }

    public LongProperty targetAccountIdProperty() {
        return targetAccountId;
    }

    public void setTargetAccountId(long targetAccountId) {
        this.targetAccountId.set(targetAccountId);
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter.get();
    }

    public ObjectProperty<BigDecimal> balanceAfterProperty() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter.set(balanceAfter);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getReferenceNumber() {
        return referenceNumber.get();
    }

    public StringProperty referenceNumberProperty() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber.set(referenceNumber);
    }

    public long getCreatedBy() {
        return createdBy.get();
    }

    public LongProperty createdByProperty() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy.set(createdBy);
    }

    public LocalDateTime getCreatedDate() {
        return createdDate.get();
    }

    public ObjectProperty<LocalDateTime> createdDateProperty() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate.set(createdDate);
    }

    // Transient Getters and Setters

    public String getAccountNumber() {
        return accountNumber.get();
    }

    public StringProperty accountNumberProperty() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber.set(accountNumber);
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber.get();
    }

    public StringProperty targetAccountNumberProperty() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber.set(targetAccountNumber);
    }

    public String getCreatedByName() {
        return createdByName.get();
    }

    public StringProperty createdByNameProperty() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName.set(createdByName);
    }
}
