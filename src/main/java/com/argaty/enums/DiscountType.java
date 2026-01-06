package com.argaty.enums;

/**
 * Enum định nghĩa loại giảm giá
 */
public enum DiscountType {
    PERCENTAGE("Phần trăm"),
    FIXED("Số tiền cố định");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}