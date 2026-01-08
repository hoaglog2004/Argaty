package com.argaty.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity CartItem - Chi tiết giỏ hàng
 */
@Entity
@Table(name = "cart_items")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "is_selected", nullable = false)
    @Builder.Default
    private Boolean isSelected = true;

    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== HELPER METHODS ==========

    /**
     * Lấy đơn giá (tính cả variant)
     */
    public BigDecimal getUnitPrice() {
        if (variant != null) {
            return variant.getFinalPrice();
        }
        return product.getEffectivePrice();
    }

    /**
     * Tính thành tiền
     */
    public BigDecimal getSubtotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Lấy tên sản phẩm hiển thị
     */
    public String getDisplayName() {
        if (variant != null) {
            return product.getName() + " - " + variant.getName();
        }
        return product.getName();
    }

    /**
     * Lấy ảnh sản phẩm
     */
    public String getImage() {
        if (variant != null && variant.getImages() != null && !variant.getImages().isEmpty()) {
            return variant.getMainImage();
        }
        return product.getMainImage();
    }

    /**
     * Kiểm tra còn hàng
     */
    public boolean isInStock() {
        if (variant != null) {
            return variant.getQuantity() >= quantity;
        }
        return product.getQuantity() >= quantity;
    }

    /**
     * Lấy số lượng tồn kho
     */
    public int getAvailableQuantity() {
        if (variant != null) {
            return variant.getQuantity();
        }
        return product.getQuantity();
    }
}