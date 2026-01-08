package com.argaty.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.argaty.dto.response.DashboardStatsResponse;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.Role;
import com.argaty.service.OrderService;
import com.argaty.service.ProductService;
import com.argaty.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Controller cho Admin Dashboard
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        
        // Build dashboard stats
        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                // Revenue
                .totalRevenue(orderService.getTotalRevenue())
                .revenueToday(orderService.getRevenueToday())
                .revenueThisMonth(orderService.getRevenueThisMonth())
                
                // Orders
                .ordersToday(orderService.countOrdersToday())
                .ordersThisMonth(orderService.countOrdersThisMonth())
                .pendingOrders(orderService.countByStatus(OrderStatus.PENDING))
                .processingOrders(orderService.countByStatus(OrderStatus.PROCESSING))
                .shippingOrders(orderService.countByStatus(OrderStatus.SHIPPING))
                .completedOrders(orderService.countByStatus(OrderStatus.COMPLETED))
                .cancelledOrders(orderService.countByStatus(OrderStatus.CANCELLED))
                
                // Products
                .totalProducts(productService.countActiveProducts())
                .outOfStockProducts(productService.countOutOfStockProducts())
                
                // Users
                .totalUsers(userService.countByRole(Role.USER))
                .newUsersToday(userService.countNewUsersToday())
                .newUsersThisMonth(userService.countNewUsersThisMonth())
                
                .build();

        // Daily statistics (last 7 days)
        List<Object[]> dailyData = orderService.getDailyStatistics(7);
        List<DashboardStatsResponse.DailyStats> dailyStats = dailyData.stream()
                .map(row -> DashboardStatsResponse.DailyStats.builder()
                        .date(row[0].toString())
                        .orderCount(((Number) row[1]).longValue())
                        .revenue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
        stats.setDailyStats(dailyStats);

        // Top selling products
        List<Object[]> topProducts = orderService.getTopSellingProducts(5);
        List<DashboardStatsResponse.TopProduct> topSellingProducts = topProducts.stream()
                .map(row -> DashboardStatsResponse.TopProduct.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .soldCount(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
        stats.setTopSellingProducts(topSellingProducts);

        // Top customers
        List<Object[]> topCustomersData = orderService.getTopCustomers(5);
        List<DashboardStatsResponse.TopCustomer> topCustomers = topCustomersData.stream()
                .map(row -> DashboardStatsResponse.TopCustomer.builder()
                        .userId(((Number) row[0]).longValue())
                        .userName((String) row[1])
                        .userEmail((String) row[2])
                        .orderCount(((Number) row[3]).longValue())
                        .totalSpent((BigDecimal) row[4])
                        .build())
                .collect(Collectors.toList());
        stats.setTopCustomers(topCustomers);

        model.addAttribute("stats", stats);
        model.addAttribute("adminPage", "dashboard");
        
        return "admin/dashboard";
    }
}