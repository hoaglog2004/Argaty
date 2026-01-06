package com.argaty.util;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class cho việc format dữ liệu
 */
public class FormatUtil {

    private static final Locale VIETNAM = new Locale("vi", "VN");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(VIETNAM);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Format số tiền VNĐ
     * VD: 1500000 -> "1.500.000 ₫"
     */
    public static String formatCurrency(long amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Format số tiền VNĐ (không có ký hiệu tiền tệ)
     * VD: 1500000 -> "1.500.000"
     */
    public static String formatNumber(long number) {
        return NumberFormat.getNumberInstance(VIETNAM).format(number);
    }

    /**
     * Format ngày
     * VD: "25/12/2024"
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMAT);
    }

    /**
     * Format ngày giờ
     * VD: "25/12/2024 14:30"
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMAT);
    }

    /**
     * Tính phần trăm giảm giá
     */
    public static int calculateDiscountPercent(long originalPrice, long salePrice) {
        if (originalPrice <= 0 || salePrice >= originalPrice) return 0;
        return (int) Math.round((1 - (double) salePrice / originalPrice) * 100);
    }
}