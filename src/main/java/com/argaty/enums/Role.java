package com.argaty.enums;

/**
 * Enum định nghĩa các vai trò người dùng
 */
public enum Role {
    USER("Khách hàng"),
    STAFF("Nhân viên"),
    ADMIN("Quản trị viên");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}