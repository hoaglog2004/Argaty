package com.argaty.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.argaty.enums.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity Voucher - Mã giảm giá
 */
@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount", precision = 15, scale = 0)
    private BigDecimal maxDiscount;

    @Column(name = "min_order_amount", precision = 15, scale = 0)
    private BigDecimal minOrderAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_limit_per_user", nullable = false)
    @Builder.Default
    private Integer usageLimitPerUser = 1;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========== HELPER METHODS ==========

    /**
     * Kiểm tra voucher còn hiệu lực
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra thời gian
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        // Kiểm tra số lần sử dụng
        if (usageLimit != null && usedCount >= usageLimit) {
            return false;
        }

        return true;
    }

    /**
     * Tính số tiền giảm
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid()) {
            return BigDecimal.ZERO;
        }

        // Kiểm tra đơn hàng tối thiểu
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));

            // Áp dụng giảm tối đa
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else {
            // FIXED
            discount = discountValue;
        }

        // Không giảm quá giá trị đơn hàng
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    /**
     * Tăng số lần sử dụng
     */
    public void incrementUsedCount() {
        this.usedCount++;
    }

    /**
     * Kiểm tra còn lượt sử dụng
     */
    public boolean hasRemainingUsage() {
        if (usageLimit == null) {
            return true;
        }
        return usedCount < usageLimit;
    }

    /**
     * Lấy số lượt sử dụng còn lại
     */
    public Integer getRemainingUsage() {
        if (usageLimit == null) {
            return null;
        }
        return Math.max(0, usageLimit - usedCount);
    }
}