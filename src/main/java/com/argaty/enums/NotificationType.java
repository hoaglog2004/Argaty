package com.argaty.enums;

/**
 * Enum định nghĩa loại thông báo
 */
public enum NotificationType {
    ORDER("Đơn hàng", "bi-box-seam"),
    PROMOTION("Khuyến mãi", "bi-gift"),
    SYSTEM("Hệ thống", "bi-bell"),
    REVIEW("Đánh giá", "bi-star");

    private final String displayName;
    private final String icon;

    NotificationType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}