package com.argaty.controller.api;

import com.argaty.dto.request.ReviewRequest;
import com. argaty.dto.response.ApiResponse;
import com.argaty.dto.response.PageResponse;
import com.argaty.dto.response.ReviewResponse;
import com.argaty.dto.response.ReviewStatsResponse;
import com.argaty.entity.Review;
import com.argaty.entity.User;
import com.argaty.exception.BadRequestException;
import com.argaty.service.ReviewService;
import com.argaty. service.UserService;
import com.argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain. Page;
import org.springframework. data.domain.PageRequest;
import org.springframework. http.ResponseEntity;
import org. springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho đánh giá
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final UserService userService;

    /**
     * Lấy đánh giá của sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating) {

        Page<Review> reviews;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviews = reviewService.findByProductIdAndRating(productId, rating, PageRequest.of(page, size));
        } else {
            reviews = reviewService.findByProductId(productId, PageRequest. of(page, size));
        }

        return ResponseEntity.ok(ApiResponse. success(DtoMapper.toReviewPageResponse(reviews)));
    }

    /**
     * Lấy thống kê đánh giá của sản phẩm
     */
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<ApiResponse<ReviewStatsResponse>> getProductReviewStats(@PathVariable Long productId) {
        Double avgRating = reviewService.getAverageRating(productId);
        long reviewCount = reviewService.getReviewCount(productId);
        List<Object[]> ratingDist = reviewService.getRatingDistribution(productId);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        for (Object[] row : ratingDist) {
            distribution.put((Integer) row[0], (Long) row[1]);
        }

        ReviewStatsResponse stats = ReviewStatsResponse.create(avgRating, reviewCount, distribution);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Tạo đánh giá mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            Principal principal) {

        User user = getCurrentUser(principal);

        try {
            Review review = reviewService.createReview(
                    user.getId(),
                    request.getProductId(),
                    request.getOrderItemId(),
                    request.getRating(),
                    request.getTitle(),
                    request.getComment(),
                    request.getImageUrls()
            );
            return ResponseEntity.ok(ApiResponse. success("Đánh giá thành công", 
                    ReviewResponse.fromEntity(review)));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e. getMessage()));
        }
    }

    /**
     * Cập nhật đánh giá
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            Principal principal) {

        User user = getCurrentUser(principal);

        try {
            Review review = reviewService.updateReview(
                    reviewId,
                    user.getId(),
                    request. getRating(),
                    request.getTitle(),
                    request.getComment()
            );
            return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công", 
                    ReviewResponse.fromEntity(review)));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa đánh giá
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            Principal principal) {

        User user = getCurrentUser(principal);
        Review review = reviewService.findById(reviewId)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Review", "id", reviewId));

        // Kiểm tra quyền
        if (!review.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Bạn không có quyền xóa đánh giá này"));
        }

        reviewService.deleteById(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa đánh giá"));
    }

    /**
     * Kiểm tra user có thể đánh giá sản phẩm không
     */
    @GetMapping("/can-review/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> canReview(
            @PathVariable Long productId,
            Principal principal) {

        User user = getCurrentUser(principal);
        boolean canReview = reviewService.canUserReviewProduct(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(canReview));
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new com.argaty.exception.UnauthorizedException("Vui lòng đăng nhập");
        }
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception. ResourceNotFoundException("User", "email", principal.getName()));
    }
}