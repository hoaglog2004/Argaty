package com.argaty.dto.response;

import com.argaty.entity.CartItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho response item trong giỏ hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private Long variantId;
    private String variantName;
    private String variantColor;
    private String variantColorCode;
    private BigDecimal unitPrice;
    private BigDecimal originalPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean isSelected;
    private Boolean isInStock;
    private Integer availableQuantity;

    public static CartItemResponse fromEntity(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSlug(item.getProduct().getSlug())
                .productImage(item.getImage())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                .variantColor(item.getVariant() != null ? item.getVariant().getColor() : null)
                .variantColorCode(item.getVariant() != null ? item.getVariant().getColorCode() : null)
                .unitPrice(item.getUnitPrice())
                .originalPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .isSelected(item.getIsSelected())
                .isInStock(item.isInStock())
                .availableQuantity(item.getAvailableQuantity())
                .build();
    }
}