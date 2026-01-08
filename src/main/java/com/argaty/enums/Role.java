package com.argaty.enums;

/**
 * Enum định nghĩa các vai trò người dùng
 */
public enum Role {
    USER("User"),
    STAFF("Staff"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}