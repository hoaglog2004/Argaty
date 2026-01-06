package com.argaty.dto.response;

import com.argaty. entity.Voucher;
import com.argaty.enums. DiscountType;
import com. fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho response voucher
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoucherResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;
    private Integer usageLimit;
    private Integer usageLimitPerUser;
    private Integer usedCount;
    private Integer remainingUsage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isValid;
    private Boolean isExpired;
    private LocalDateTime createdAt;

    // Thông tin hiển thị
    private String discountDisplay;

    public static VoucherResponse fromEntity(Voucher voucher) {
        String discountDisplay;
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            discountDisplay = String.format("Giảm %s%%", voucher.getDiscountValue().intValue());
            if (voucher.getMaxDiscount() != null) {
                discountDisplay += String.format(" (tối đa %,dđ)", voucher.getMaxDiscount().longValue());
            }
        } else {
            discountDisplay = String.format("Giảm %,dđ", voucher.getDiscountValue().longValue());
        }

        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscount(voucher.getMaxDiscount())
                .minOrderAmount(voucher. getMinOrderAmount())
                .usageLimit(voucher.getUsageLimit())
                .usageLimitPerUser(voucher.getUsageLimitPerUser())
                .usedCount(voucher.getUsedCount())
                .remainingUsage(voucher.getRemainingUsage())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .isActive(voucher.getIsActive())
                .isValid(voucher.isValid())
                .isExpired(voucher.getEndDate() != null && LocalDateTime.now().isAfter(voucher.getEndDate()))
                .createdAt(voucher.getCreatedAt())
                .discountDisplay(discountDisplay)
                .build();
    }
}