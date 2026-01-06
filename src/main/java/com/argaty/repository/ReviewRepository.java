package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data. domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data. repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Review;

/**
 * Repository cho Review Entity
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ========== FIND BY PRODUCT ==========

    Page<Review> findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<Review> findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(Long productId);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isVisible = true " +
           "AND r.rating = :rating ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndRating(@Param("productId") Long productId,
                                          @Param("rating") int rating,
                                          Pageable pageable);

    // ========== FIND BY USER ==========

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // ========== FIND BY ORDER ITEM ==========

    Optional<Review> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

    // ========== STATISTICS ==========

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isVisible = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isVisible = true")
    long countByProductId(@Param("productId") Long productId);

    @Query("SELECT r. rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isVisible = true " +
           "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    // ========== FIND WITH IMAGES ==========

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isVisible = true " +
           "AND SIZE(r.images) > 0 ORDER BY r.createdAt DESC")
    Page<Review> findReviewsWithImages(@Param("productId") Long productId, Pageable pageable);

    // ========== ADMIN ==========

    Page<Review> findByIsVisibleFalse(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.reply IS NULL AND r.isVisible = true ORDER BY r.createdAt ASC")
    List<Review> findUnrepliedReviews(Pageable pageable);
}