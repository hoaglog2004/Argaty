package com.argaty.service;

import com.argaty.entity.Order;
import com.argaty.entity.OrderItem;
import com.argaty.entity.User;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Order
 */
public interface OrderService {

    // ========== CRUD ==========

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByIdWithDetails(Long id);

    Page<Order> findAll(Pageable pageable);

    // ========== USER ORDERS ==========

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId);

    // ========== CREATE ORDER ==========

    Order createOrder(Long userId, String receiverName, String receiverPhone,
                      String receiverEmail, String shippingAddress,
                      String city, String district, String ward,
                      PaymentMethod paymentMethod, String voucherCode, String note);

    Order createOrderFromCart(Long userId, Long cartId, String receiverName,
                              String receiverPhone, String receiverEmail,
                              String shippingAddress, String city,
                              String district, String ward,
                              PaymentMethod paymentMethod, String voucherCode, String note);

    // ========== ORDER STATUS ==========

    Order updateStatus(Long orderId, OrderStatus newStatus, User changedBy, String note);

    Order confirmOrder(Long orderId, User changedBy);

    Order shipOrder(Long orderId, User changedBy, String note);

    Order deliverOrder(Long orderId, User changedBy);

    Order completeOrder(Long orderId, User changedBy);

    Order cancelOrder(Long orderId, User changedBy, String reason);

    Order requestReturn(Long orderId, User user, String reason);

    Order approveReturn(Long orderId, User changedBy, String note);

    // ========== PAYMENT ==========

    Order updatePaymentStatus(Long orderId, boolean isPaid, String transactionId);

    // ========== SEARCH & FILTER ==========

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> searchOrders(String keyword, Pageable pageable);

    Page<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ========== STATISTICS ==========

    long countByStatus(OrderStatus status);

    long countByUserId(Long userId);

    long countOrdersToday();

    long countOrdersThisMonth();

    BigDecimal getTotalRevenue();

    BigDecimal getRevenueToday();

    BigDecimal getRevenueThisMonth();

    BigDecimal getTotalSpentByUser(Long userId);

    List<Object[]> getDailyStatistics(int days);

    List<Object[]> getMonthlyStatistics();

    List<Object[]> getTopSellingProducts(int limit);

    List<Object[]> getTopCustomers(int limit);
}