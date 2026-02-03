package com.customer.model;

public enum Role {
    ADMIN("Administrator"),
    MANAGER("Manager"),
    STAFF("Staff");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
