package com. argaty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ProductVariant - Phân loại sản phẩm (màu sắc, switch type, v.v.)
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sku", length = 50)
    private String sku;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "additional_price", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType. LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantImage> images = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Lấy giá cuối cùng (giá sản phẩm + giá cộng thêm)
     */
    public BigDecimal getFinalPrice() {
        if (product == null) {
            return additionalPrice;
        }
        return product.getEffectivePrice().add(additionalPrice);
    }

    /**
     * Kiểm tra còn hàng
     */
    public boolean isInStock() {
        return quantity > 0;
    }

    /**
     * Lấy ảnh chính của variant
     */
    public String getMainImage() {
        if (images == null || images.isEmpty()) {
            return product != null ? product.getMainImage() : "/static/images/no-image.png";
        }

        return images.stream()
                .filter(VariantImage:: getIsMain)
                .findFirst()
                .map(VariantImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }
}