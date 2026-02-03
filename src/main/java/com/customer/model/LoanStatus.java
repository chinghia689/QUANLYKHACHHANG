package com.customer.model;

public enum LoanStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    DISBURSED("Đã giải ngân"),
    PAID("Đã thanh toán"),
    OVERDUE("Quá hạn");

    private final String displayName;

    LoanStatus(String displayName) {
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
