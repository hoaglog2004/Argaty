package com.argaty.dto.response;

import com.argaty.entity.Wishlist;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho response wishlist
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistResponse {

    private Long id;
    private LocalDateTime createdAt;

    // Product info
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private Boolean isOnSale;
    private Boolean isInStock;

    public static WishlistResponse fromEntity(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .createdAt(wishlist.getCreatedAt())
                .productId(wishlist.getProduct().getId())
                .productName(wishlist.getProduct().getName())
                .productSlug(wishlist.getProduct().getSlug())
                .productImage(wishlist.getProduct().getMainImage())
                .price(wishlist.getProduct().getPrice())
                .salePrice(wishlist.getProduct().getSalePrice())
                .discountPercent(wishlist.getProduct().getCalculatedDiscountPercent())
                .isOnSale(wishlist.getProduct().isOnSale())
                .isInStock(wishlist.getProduct().isInStock())
                .build();
    }
}