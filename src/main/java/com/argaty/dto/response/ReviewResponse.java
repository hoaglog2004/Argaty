package com.argaty.dto.response;

import com.argaty.entity.Review;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO cho response đánh giá
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerified;
    private LocalDateTime createdAt;

    // User info
    private Long userId;
    private String userName;
    private String userAvatar;

    // Images
    private List<String> images;

    // Reply
    private String reply;
    private LocalDateTime repliedAt;
    private String repliedByName;

    // Product info (cho trang profile)
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImage;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerified(review.getIsVerified())
                .createdAt(review.getCreatedAt())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .userAvatar(review.getUser().getAvatar())
                .reply(review.getReply())
                .repliedAt(review.getRepliedAt())
                .repliedByName(review.getRepliedBy() != null ? review.getRepliedBy().getFullName() : null)
                .build();

        // Images
        if (review.getImages() != null) {
            response.setImages(review.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList()));
        }

        // Product info
        if (review.getProduct() != null) {
            response.setProductId(review.getProduct().getId());
            response.setProductName(review.getProduct().getName());
            response.setProductSlug(review.getProduct().getSlug());
            response.setProductImage(review.getProduct().getMainImage());
        }

        return response;
    }
}