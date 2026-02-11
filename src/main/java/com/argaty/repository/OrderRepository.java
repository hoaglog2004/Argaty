package com.argaty.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Order;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;

/**
 * Repository cho Order Entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // ========== FIND BY FIELD ==========

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId);

    boolean existsByOrderCode(String orderCode);

    // ========== FIND BY USER ==========

    // [QUAN TRỌNG] Đã đổi tên từ findByUserIdOrderByCreatedAtDesc -> findByUserId
    // Để tránh bị trùng lặp ORDER BY với Pageable truyền vào
    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    // [SỬA] Bỏ ORDER BY cứng, để Pageable tự xử lý sắp xếp
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId, Pageable pageable);

    // ========== FIND BY STATUS ==========

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders(@Param("status") OrderStatus status);

    // ========== FIND BY PAYMENT ==========

    Page<Order> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    Page<Order> findByIsPaidFalseAndPaymentMethodNot(PaymentMethod excludeMethod, Pageable pageable);

    // ========== SEARCH ==========

    @Query("SELECT o FROM Order o WHERE " +
            "o.orderCode LIKE CONCAT('%', :keyword, '%') OR " +
            "o.receiverName LIKE CONCAT('%', :keyword, '%') OR " +
            "o.receiverPhone LIKE CONCAT('%', :keyword, '%')")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

    // [SỬA] Fix lỗi khoảng trắng ": keyword" thành ":keyword"
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND " +
            "(o.orderCode LIKE CONCAT('%', :keyword, '%') OR " +
            "o.receiverName LIKE CONCAT('%', :keyword, '%'))")
    Page<Order> searchUserOrders(@Param("userId") Long userId,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    // ========== FIND BY DATE RANGE ==========

    // [SỬA] Fix lỗi khoảng trắng ": startDate" thành ":startDate"
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.status = :status")
    List<Order> findByDateAndStatus(@Param("startDate") LocalDateTime startDate,
                                    @Param("status") OrderStatus status);

    // ========== FETCH WITH ITEMS ==========

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeWithItems(@Param("orderCode") String orderCode);

    // ========== UPDATE ==========

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    void updateStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

    // [SỬA] Fix lỗi khoảng trắng ": paidAt", ": orderId"
    @Modifying
    @Query("UPDATE Order o SET o.isPaid = true, o.paidAt = :paidAt, " +
            "o.paymentTransactionId = :transactionId WHERE o.id = :orderId")
    void updatePaymentStatus(@Param("orderId") Long orderId,
                             @Param("paidAt") LocalDateTime paidAt,
                             @Param("transactionId") String transactionId);

    // ========== STATISTICS ==========

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    long countOrdersFromDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);

    // ========== REVENUE STATISTICS ==========

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt >= :startDate")
    BigDecimal getRevenueFromDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    BigDecimal getTotalSpentByUser(@Param("userId") Long userId);

    // ========== DAILY/MONTHLY STATISTICS ==========

    @Query(value = "SELECT CAST(o.created_at AS DATE) as orderDate, COUNT(*) as orderCount, SUM(o.total_amount) as revenue " +
            "FROM orders o WHERE o.status = 'COMPLETED' AND o.created_at >= :startDate " +
            "GROUP BY CAST(o.created_at AS DATE) ORDER BY orderDate DESC",
            nativeQuery = true)
    List<Object[]> getDailyStatistics(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT YEAR(o.created_at) as year, MONTH(o.created_at) as month, " +
            "COUNT(*) as orderCount, SUM(o.total_amount) as revenue " +
            "FROM orders o WHERE o.status = 'COMPLETED' " +
            "GROUP BY YEAR(o.created_at), MONTH(o.created_at) " +
            "ORDER BY year DESC, month DESC",
            nativeQuery = true)
    List<Object[]> getMonthlyStatistics();

    // ========== TOP CUSTOMERS ==========

    @Query("SELECT o.user.id, o.user.fullName, o.user.email, COUNT(o), SUM(o.totalAmount) " +
            "FROM Order o WHERE o.status = 'COMPLETED' " +
            "GROUP BY o.user.id, o.user.fullName, o.user.email " +
            "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> getTopCustomers(Pageable pageable);
}