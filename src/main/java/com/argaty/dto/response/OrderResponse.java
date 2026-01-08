package com.argaty.dto.response;

import com.argaty.entity.Order;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho response danh sách đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private Long id;
    private String orderCode;
    private OrderStatus status;
    private String statusDisplayName;
    private String statusBadgeClass;
    private PaymentMethod paymentMethod;
    private String paymentMethodDisplayName;
    private Boolean isPaid;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private String firstProductImage;
    private String firstProductName;
    private LocalDateTime createdAt;

    public static OrderResponse fromEntity(Order order) {
        String firstImage = null;
        String firstName = null;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            firstImage = order.getItems().get(0).getProductImage();
            firstName = order.getItems().get(0).getProductName();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusDisplayName(order.getStatus().getDisplayName())
                .statusBadgeClass(order.getStatus().getBadgeClass())
                .paymentMethod(order.getPaymentMethod())
                .paymentMethodDisplayName(order.getPaymentMethod().getDisplayName())
                .isPaid(order.getIsPaid())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItemCount())
                .firstProductImage(firstImage)
                .firstProductName(firstName)
                .createdAt(order.getCreatedAt())
                .build();
    }
}