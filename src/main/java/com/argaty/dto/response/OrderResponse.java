package com.argaty.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.argaty.entity.Order;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private Long id;
    private String orderCode;
    
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    
    private String fullAddress;
    private String note;
    
    private OrderStatus status;
    private String statusDisplayName;
    private String statusBadgeClass;
    
    private PaymentMethod paymentMethod;
    private String paymentMethodDisplayName;
    
    private Boolean isPaid;
    private LocalDateTime paidAt;
    
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Integer totalItems;

    private String firstProductImage;
    private String firstProductName;
    
    // --- KHAI BÁO RÕ RÀNG TÊN CLASS CHA ---
    private List<OrderResponse.OrderItemResponse> items; 
    private List<OrderResponse.OrderHistoryResponse> histories;

    private LocalDateTime createdAt;

    // --- Inner Class: Item ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String productName;
        private String productImage;
        private String variantName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private Boolean isReviewed;
    }

    // --- Inner Class: History ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderHistoryResponse {
        private String description;
        private LocalDateTime createdAt;
    }

    // --- HÀM MAP DỮ LIỆU ---
    public static OrderResponse fromEntity(Order order) {
        
        String firstImage = null;
        String firstName = null;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            firstImage = order.getItems().get(0).getProductImage();
            firstName = order.getItems().get(0).getProductName();
        }
        int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;
        // 1. Map Items (FIX LỖI ĐỎ: Gọi đầy đủ OrderResponse.OrderItemResponse)
        List<OrderResponse.OrderItemResponse> itemResponses = null;
        
        if (order.getItems() != null) {
            itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .variantName(item.getVariantName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .isReviewed(false)
                        .build())
                .collect(Collectors.toList());
        }

        // 2. Map History (FIX LỖI ĐỎ: Gọi đầy đủ OrderResponse.OrderHistoryResponse)
        List<OrderResponse.OrderHistoryResponse> historyResponses = null;
        
        if (order.getStatusHistory() != null) {
            historyResponses = order.getStatusHistory().stream()
                .map(h -> OrderResponse.OrderHistoryResponse.builder()
                        .description(h.getStatus() != null ? h.getStatus().name() : "") // Use getStatus() as description
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .fullAddress((order.getShippingAddress() != null ? order.getShippingAddress() : "") + ", " + 
                             (order.getWard() != null ? order.getWard() : "") + ", " + 
                             (order.getDistrict() != null ? order.getDistrict() : "") + ", " + 
                             (order.getCity() != null ? order.getCity() : ""))
                .note(order.getNote())
                
                .status(order.getStatus())
                .statusDisplayName(getStatusName(order.getStatus()))
                .statusBadgeClass("status-badge--" + (order.getStatus() != null ? order.getStatus().name().toLowerCase() : ""))
                
                .paymentMethod(order.getPaymentMethod())
                .paymentMethodDisplayName(getPaymentMethodName(order.getPaymentMethod()))
                
                .isPaid(order.getIsPaid())
                .paidAt(order.getPaidAt())
                
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .totalItems(itemCount)

                .firstProductImage(firstImage)
                .firstProductName(firstName)
                
                .items(itemResponses)       
                .histories(historyResponses)
                
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    private static String getStatusName(OrderStatus status) {
        if(status == null) return "";
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPING: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }

    private static String getPaymentMethodName(PaymentMethod method) {
        if(method == null) return "";
        switch (method) {
            case COD: return "Thanh toán khi nhận (COD)";
            case VNPAY: return "VNPay";
            default: return method.name();
        }
    }
}