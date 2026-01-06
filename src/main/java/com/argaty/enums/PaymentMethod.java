package com.argaty.enums;

/**
 * Enum định nghĩa phương thức thanh toán
 */
public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng", "bi-cash-stack"),
    BANK_TRANSFER("Chuyển khoản ngân hàng", "bi-bank"),
    MOMO("Ví MoMo", "bi-wallet2"),
    VNPAY("VNPay", "bi-credit-card"),
    ZALOPAY("ZaloPay", "bi-wallet");

    private final String displayName;
    private final String icon;

    PaymentMethod(String displayName, String icon) {
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