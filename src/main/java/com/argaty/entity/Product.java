package com.argaty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity Product - Sản phẩm
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "sku", unique = true, length = 50)
    private String sku;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "price", nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Column(name = "sale_price", precision = 15, scale = 0)
    private BigDecimal salePrice;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "sold_count", nullable = false)
    @Builder.Default
    private Integer soldCount = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_new", nullable = false)
    @Builder.Default
    private Boolean isNew = true;

    @Column(name = "is_best_seller", nullable = false)
    @Builder.Default
    private Boolean isBestSeller = false;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "specifications", columnDefinition = "NVARCHAR(MAX)")
    private String specifications;

    @Column(name = "sale_start_date")
    private LocalDateTime saleStartDate;

    @Column(name = "sale_end_date")
    private LocalDateTime saleEndDate;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("displayOrder ASC")
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("displayOrder ASC")
    private Set<ProductVariant> variants = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private List<Review> reviews = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Lấy giá hiệu lực (giá sale nếu có, ngược lại giá gốc)
     */
    public BigDecimal getEffectivePrice() {
        if (salePrice != null && isOnSale()) {
            return salePrice;
        }
        return price;
    }

    /**
     * Kiểm tra sản phẩm đang sale
     */
    public boolean isOnSale() {
        if (salePrice == null || salePrice.compareTo(price) >= 0) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra thời gian sale
        if (saleStartDate != null && now.isBefore(saleStartDate)) {
            return false;
        }
        if (saleEndDate != null && now.isAfter(saleEndDate)) {
            return false;
        }

        return true;
    }

    /**
     * Tính phần trăm giảm giá
     */
    public int getCalculatedDiscountPercent() {
        if (!isOnSale() || price.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal discount = price.subtract(salePrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(price, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * Lấy ảnh chính của sản phẩm
     */
    public String getMainImage() {
        if (images == null || images.isEmpty()) {
            return "/static/images/no-image.png";
        }

        return images.stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElseGet(() -> images.iterator().next().getImageUrl());
    }

    /**
     * Kiểm tra còn hàng
     */
    public boolean isInStock() {
        return quantity > 0;
    }

    /**
     * Kiểm tra sắp hết hàng
     */
    public boolean isLowStock() {
        return quantity > 0 && quantity <= lowStockThreshold;
    }

    /**
     * Kiểm tra hết hàng
     */
    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    /**
     * Lấy tổng số lượng từ tất cả variants
     */
    public int getTotalVariantQuantity() {
        if (variants == null || variants.isEmpty()) {
            return quantity;
        }
        return variants.stream()
                .filter(ProductVariant::getIsActive)
                .mapToInt(ProductVariant::getQuantity)
                .sum();
    }

    /**
     * Thêm ảnh vào sản phẩm
     */
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    /**
     * Xóa ảnh khỏi sản phẩm
     */
    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    /**
     * Thêm variant vào sản phẩm
     */
    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    /**
     * Xóa variant khỏi sản phẩm
     */
    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }
}