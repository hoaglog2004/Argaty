package com.argaty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Review - Đánh giá sản phẩm
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "reply", columnDefinition = "NVARCHAR(MAX)")
    private String reply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by")
    private User repliedBy;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Kiểm tra đã có phản hồi chưa
     */
    public boolean hasReply() {
        return reply != null && !reply.isEmpty();
    }

    /**
     * Thêm ảnh vào đánh giá
     */
    public void addImage(ReviewImage image) {
        images.add(image);
        image.setReview(this);
    }
}