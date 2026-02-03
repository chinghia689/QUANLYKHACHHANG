package com.customer.model;

public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    LOCKED("Locked");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
