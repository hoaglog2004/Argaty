package com.argaty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho response thống kê dashboard (Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    // Thống kê tổng quan
    private BigDecimal totalRevenue;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueGrowth; // % so với tháng trước

    // Đơn hàng
    private Long totalOrders;
    private Long ordersToday;
    private Long ordersThisMonth;
    private Long pendingOrders;
    private Long processingOrders;
    private Long shippingOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    // Sản phẩm
    private Long totalProducts;
    private Long activeProducts;
    private Long outOfStockProducts;
    private Long lowStockProducts;

    // Người dùng
    private Long totalUsers;
    private Long newUsersToday;
    private Long newUsersThisMonth;
    private Long activeUsers;

    // Thống kê theo thời gian
    private List<DailyStats> dailyStats;
    private List<MonthlyStats> monthlyStats;

    // Top sản phẩm
    private List<TopProduct> topSellingProducts;

    // Top khách hàng
    private List<TopCustomer> topCustomers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        private String date;
        private Long orderCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStats {
        private Integer year;
        private Integer month;
        private String monthName;
        private Long orderCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String productImage;
        private Long soldCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long userId;
        private String userName;
        private String userEmail;
        private String userAvatar;
        private Long orderCount;
        private BigDecimal totalSpent;
    }
}