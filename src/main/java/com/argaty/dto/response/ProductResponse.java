package com.argaty. dto.response;

import com. argaty.entity.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho response danh sách sản phẩm (card)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer quantity;
    private Boolean isNew;
    private Boolean isFeatured;
    private Boolean isBestSeller;
    private Boolean isOnSale;
    private Boolean isInStock;

    // Category & Brand
    private String categoryName;
    private String categorySlug;
    private String brandName;
    private String brandSlug;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse. builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .mainImage(product.getMainImage())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .discountPercent(product.getCalculatedDiscountPercent())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .quantity(product.getQuantity())
                .isNew(product.getIsNew())
                .isFeatured(product. getIsFeatured())
                .isBestSeller(product.getIsBestSeller())
                .isOnSale(product.isOnSale())
                .isInStock(product.isInStock())
                .categoryName(product. getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .brandName(product. getBrand() != null ? product.getBrand().getName() : null)
                .brandSlug(product.getBrand() != null ? product.getBrand().getSlug() : null)
                .build();
    }
}