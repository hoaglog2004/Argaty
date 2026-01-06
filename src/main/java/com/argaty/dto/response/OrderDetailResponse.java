package com.argaty.dto.response;

import com.argaty.entity.Order;
import com.argaty. entity.OrderItem;
import com.argaty.entity.OrderStatusHistory;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream. Collectors;

/**
 * DTO cho response chi tiết đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {

    private Long id;
    private String orderCode;

    // Trạng thái
    private OrderStatus status;
    private String statusDisplayName;
    private String statusBadgeClass;

    // Thông tin người nhận
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String fullAddress;
    private String shippingAddress;
    private String city;
    private String district;
    private String ward;

    // Thanh toán
    private PaymentMethod paymentMethod;
    private String paymentMethodDisplayName;
    private Boolean isPaid;
    private LocalDateTime paidAt;
    private String paymentTransactionId;

    // Giá trị
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String voucherCode;

    // Ghi chú
    private String note;
    private String adminNote;
    private String cancelReason;
    private String returnReason;

    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    // Actions
    private Boolean canCancel;
    private Boolean canRequestReturn;

    // Items
    private List<OrderItemResponse> items;

    // History
    private List<StatusHistoryResponse> statusHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private String productSlug;
        private String variantName;
        private String sku;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private Boolean isReviewed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistoryResponse {
        private OrderStatus status;
        private String statusDisplayName;
        private String note;
        private String changedByName;
        private LocalDateTime createdAt;
    }

    public static OrderDetailResponse fromEntity(Order order) {
        OrderDetailResponse response = OrderDetailResponse. builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusDisplayName(order.getStatus().getDisplayName())
                .statusBadgeClass(order.getStatus().getBadgeClass())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .fullAddress(order.getFullAddress())
                .shippingAddress(order. getShippingAddress())
                .city(order.getCity())
                .district(order.getDistrict())
                .ward(order.getWard())
                .paymentMethod(order. getPaymentMethod())
                .paymentMethodDisplayName(order. getPaymentMethod().getDisplayName())
                .isPaid(order.getIsPaid())
                .paidAt(order.getPaidAt())
                .paymentTransactionId(order.getPaymentTransactionId())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .voucherCode(order. getVoucherCode())
                .note(order.getNote())
                .adminNote(order. getAdminNote())
                .cancelReason(order.getCancelReason())
                .returnReason(order.getReturnReason())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order. getDeliveredAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .canCancel(order.canCancel())
                .canRequestReturn(order.canRequestReturn())
                .build();

        // Items
        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProductName())
                            .productImage(item.getProductImage())
                            .productSlug(item.getProduct().getSlug())
                            .variantName(item.getVariantName())
                            .sku(item.getSku())
                            .unitPrice(item.getUnitPrice())
                            . quantity(item.getQuantity())
                            .subtotal(item.getSubtotal())
                            .isReviewed(item.getIsReviewed())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Status History
        if (order.getStatusHistory() != null) {
            response.setStatusHistory(order. getStatusHistory().stream()
                    .map(h -> StatusHistoryResponse.builder()
                            .status(h. getStatus())
                            .statusDisplayName(h.getStatus().getDisplayName())
                            . note(h.getNote())
                            .changedByName(h.getChangedBy() != null ? h.getChangedBy().getFullName() : "Hệ thống")
                            .createdAt(h.getCreatedAt())
                            . build())
                    .collect(Collectors.toList()));
        }

        return response;
    }
}