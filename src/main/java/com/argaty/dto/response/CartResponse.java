package com.argaty.dto.response;

import com.argaty.entity.Cart;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream. Collectors;

/**
 * DTO cho response giỏ hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private Long id;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer selectedItems;
    private BigDecimal totalAmount;
    private BigDecimal selectedAmount;

    public static CartResponse fromEntity(Cart cart) {
        List<CartItemResponse> items = cart.getItems() != null ?
                cart.getItems().stream()
                        . map(CartItemResponse::fromEntity)
                        .collect(Collectors.toList()) : List.of();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalItems(cart.getTotalItemCount())
                .selectedItems(cart.getSelectedItemCount())
                .totalAmount(cart.getAllItemsTotal())
                .selectedAmount(cart.getTotalAmount())
                .build();
    }
}