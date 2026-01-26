package com.customer.model;

public enum CustomerType {
    VIP("VIP"),
    REGULAR("Thường"),
    POTENTIAL("Tiềm năng");

    private final String displayName;

    CustomerType(String displayName) {
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
