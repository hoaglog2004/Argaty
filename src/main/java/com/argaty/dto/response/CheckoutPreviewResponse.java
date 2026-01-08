package com.argaty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho response preview checkout (tính toán giá trước khi đặt hàng)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutPreviewResponse {

    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // Voucher info
    private String voucherCode;
    private String voucherName;
    private String voucherDiscount;
    private Boolean voucherApplied;
    private String voucherError;

    // Shipping info
    private Boolean freeShipping;
    private BigDecimal freeShippingThreshold;
    private BigDecimal amountToFreeShipping;

    // Available vouchers
    private List<VoucherResponse> availableVouchers;
}