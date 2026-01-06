package com.argaty.service.impl;

import com.argaty. entity.*;
import com.argaty.exception.ResourceNotFoundException;
import com. argaty.exception.BadRequestException;
import com.argaty.repository.*;
import com. argaty.service.ProductService;
import com.argaty.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework. data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java. util.List;
import java. util.Optional;

/**
 * Implementation của ReviewService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByProductId(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(productId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByProductIdAndRating(Long productId, int rating, Pageable pageable) {
        return reviewRepository.findByProductIdAndRating(productId, rating, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public void deleteById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        Long productId = review.getProduct().getId();
        reviewRepository.deleteById(id);

        // Cập nhật rating của sản phẩm
        productService.updateRating(productId);

        log.info("Deleted review: {}", id);
    }

    // ========== CREATE & UPDATE ==========

    @Override
    public Review createReview(Long userId, Long productId, Long orderItemId,
                               int rating, String title, String comment, List<String> imageUrls) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Kiểm tra đã review chưa
        if (reviewRepository. existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Validate rating
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Đánh giá phải từ 1 đến 5 sao");
        }

        OrderItem orderItem = null;
        boolean isVerified = false;

        if (orderItemId != null) {
            orderItem = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));

            // Kiểm tra order item thuộc về user và đã completed
            if (!orderItem.getOrder().getUser().getId().equals(userId)) {
                throw new BadRequestException("Bạn không có quyền đánh giá sản phẩm này");
            }

            if (! orderItem.getOrder().isCompleted()) {
                throw new BadRequestException("Đơn hàng chưa hoàn thành");
            }

            isVerified = true;

            // Đánh dấu order item đã review
            orderItemRepository.markAsReviewed(orderItemId);
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .orderItem(orderItem)
                .rating(rating)
                .title(title)
                .comment(comment)
                .isVerified(isVerified)
                .isVisible(true)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Thêm ảnh nếu có
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ReviewImage image = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(imageUrl)
                        .build();
                reviewImageRepository.save(image);
            }
        }

        // Cập nhật rating của sản phẩm
        productService.updateRating(productId);

        log.info("Created review for product {} by user {}", productId, userId);
        return savedReview;
    }

    @Override
    public Review updateReview(Long reviewId, Long userId, int rating, String title, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Kiểm tra quyền
        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền sửa đánh giá này");
        }

        review.setRating(rating);
        review.setTitle(title);
        review.setComment(comment);

        Review savedReview = reviewRepository.save(review);

        // Cập nhật rating của sản phẩm
        productService.updateRating(review. getProduct().getId());

        log.info("Updated review: {}", reviewId);
        return savedReview;
    }

    @Override
    public Review replyToReview(Long reviewId, Long adminId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        review.setReply(reply);
        review.setRepliedAt(LocalDateTime.now());
        review.setRepliedBy(admin);

        log.info("Admin {} replied to review {}", adminId, reviewId);
        return reviewRepository.save(review);
    }

    // ========== VALIDATION ==========

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Kiểm tra đã review chưa
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            return false;
        }

        // Kiểm tra đã mua sản phẩm chưa
        return orderItemRepository.hasUserPurchasedProduct(userId, productId);
    }

    // ========== STATISTICS ==========

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getRatingDistribution(Long productId) {
        return reviewRepository.getRatingDistribution(productId);
    }

    // ========== ADMIN ==========

    @Override
    public void toggleVisibility(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setIsVisible(!review.getIsVisible());
        reviewRepository.save(review);

        // Cập nhật rating của sản phẩm
        productService.updateRating(review. getProduct().getId());

        log.info("Toggled review visibility: {} -> {}", reviewId, review.getIsVisible());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findUnrepliedReviews(Pageable pageable) {
        List<Review> reviews = reviewRepository.findUnrepliedReviews(pageable);
        return new org.springframework.data.domain.PageImpl<>(reviews, pageable, reviews.size());
    }
}