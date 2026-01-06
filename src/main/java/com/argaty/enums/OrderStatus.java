package com.argaty.enums;

/**
 * Enum định nghĩa trạng thái đơn hàng
 */
public enum OrderStatus {
    PENDING("Chờ xác nhận", "warning"),
    CONFIRMED("Đã xác nhận", "info"),
    PROCESSING("Đang xử lý", "info"),
    SHIPPING("Đang giao hàng", "primary"),
    DELIVERED("Đã giao hàng", "success"),
    COMPLETED("Hoàn thành", "success"),
    CANCELLED("Đã hủy", "danger"),
    RETURN_REQUESTED("Yêu cầu đổi trả", "warning"),
    RETURNED("Đã đổi trả", "secondary");

    private final String displayName;
    private final String badgeClass; // Bootstrap badge class

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}