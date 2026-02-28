package com.argaty.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.OrderItem;

/**
 * Repository cho OrderItem Entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.product WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithProduct(@Param("orderId") Long orderId);

    // Tìm các sản phẩm đã mua chưa review
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId " +
           "AND oi.isReviewed = false AND oi.order.status = 'COMPLETED'")
    List<OrderItem> findUnreviewedItemsByUserId(@Param("userId") Long userId);

    // Kiểm tra user đã mua sản phẩm chưa
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.order.user.id = :userId " +
           "AND oi.product.id = :productId AND oi.order.status = 'COMPLETED'")
    boolean hasUserPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.isReviewed = true WHERE oi.id = :itemId")
    void markAsReviewed(@Param("itemId") Long itemId);

       @Modifying
       @Query("DELETE FROM OrderItem oi WHERE oi.product.id = :productId")
       void deleteByProductId(@Param("productId") Long productId);

    // ========== STATISTICS ==========

    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi WHERE oi.order.status = 'COMPLETED' " +
           "GROUP BY oi.product.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProducts(Pageable pageable);

    @Query("SELECT oi.product.category.id, oi.product.category.name, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi WHERE oi.order.status = 'COMPLETED' " +
           "GROUP BY oi.product.category.id, oi.product.category.name " +
           "ORDER BY totalSold DESC")
    List<Object[]> getSalesByCategory();
}