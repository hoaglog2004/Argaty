package com.argaty.service;

import com.argaty.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Review
 */
public interface ReviewService {

    Review save(Review review);

    Optional<Review> findById(Long id);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByProductIdAndRating(Long productId, int rating, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    void deleteById(Long id);

    // ========== CREATE & UPDATE ==========

    Review createReview(Long userId, Long productId, Long orderItemId,
                        int rating, String title, String comment, List<String> imageUrls);

    Review updateReview(Long reviewId, Long userId, int rating, String title, String comment);

    Review replyToReview(Long reviewId, Long adminId, String reply);

    // ========== VALIDATION ==========

    boolean hasUserReviewedProduct(Long userId, Long productId);

    boolean canUserReviewProduct(Long userId, Long productId);

    // ========== STATISTICS ==========

    Double getAverageRating(Long productId);

    long getReviewCount(Long productId);

    List<Object[]> getRatingDistribution(Long productId);

    // ========== ADMIN ==========

    void toggleVisibility(Long reviewId);

    Page<Review> findUnrepliedReviews(Pageable pageable);
}