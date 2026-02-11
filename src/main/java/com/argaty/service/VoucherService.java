package com.argaty.service;

import com.argaty.entity.Voucher;
import com.argaty.enums.DiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Voucher
 */
public interface VoucherService {

    // ========== CRUD ==========

    Voucher save(Voucher voucher);

    Optional<Voucher> findById(Long id);

    Optional<Voucher> findByCode(String code);

    Page<Voucher> findAll(Pageable pageable);

    Page<Voucher> search(String keyword, Pageable pageable);

    void deleteById(Long id);

    boolean existsByCode(String code);

    // ========== CREATE & UPDATE ==========

    Voucher create(String code, String name, String description,
                   DiscountType discountType, BigDecimal discountValue,
                   BigDecimal maxDiscount, BigDecimal minOrderAmount,
                   Integer usageLimit, Integer usageLimitPerUser,
                   LocalDateTime startDate, LocalDateTime endDate, Boolean isActive);

    Voucher update(Long id, String name, String description,
                   DiscountType discountType, BigDecimal discountValue,
                   BigDecimal maxDiscount, BigDecimal minOrderAmount,
                   Integer usageLimit, Integer usageLimitPerUser,
                   LocalDateTime startDate, LocalDateTime endDate, Boolean isActive);

    void toggleActive(Long id);

    // ========== VALIDATION & APPLY ==========

    boolean isVoucherValid(String code);

    boolean canUserUseVoucher(String code, Long userId);

    BigDecimal calculateDiscount(String code, BigDecimal orderAmount);

    Voucher applyVoucher(String code, Long userId, Long orderId);

    // ========== FIND VOUCHERS ==========

    List<Voucher> findValidVouchers();

    List<Voucher> findApplicableVouchers(BigDecimal orderAmount);

    List<Voucher> findVouchersForUser(Long userId, BigDecimal orderAmount);

    // ========== MAINTENANCE ==========

    int deactivateExpiredVouchers();
}