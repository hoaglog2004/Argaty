package com.argaty.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class để tạo mã đơn hàng
 */
public class OrderCodeGenerator {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");

    /**
     * Tạo mã đơn hàng
     * Format: AG + yyMMddHHmm + 3 số random
     * VD: AG2412251430001
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(formatter);
        int count = counter.incrementAndGet() % 1000;
        return String.format("AG%s%03d", timestamp, count);
    }
}