package com.customer.model;

public enum TransactionType {
    DEPOSIT("Nạp tiền"),
    WITHDRAW("Rút tiền"),
    TRANSFER("Chuyển khoản"),
    LOAN_DISBURSEMENT("Giải ngân"),
    LOAN_PAYMENT("Thanh toán nợ");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
