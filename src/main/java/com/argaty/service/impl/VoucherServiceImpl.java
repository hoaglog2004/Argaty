package com.argaty.service.impl;

import com.argaty.entity.Order;
import com.argaty.entity.User;
import com.argaty.entity.Voucher;
import com.argaty.entity.VoucherUsage;
import com.argaty.enums.DiscountType;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.exception.BadRequestException;
import com.argaty.repository.OrderRepository;
import com.argaty.repository.UserRepository;
import com.argaty.repository.VoucherRepository;
import com.argaty.repository.VoucherUsageRepository;
import com.argaty.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation của VoucherService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ========== CRUD ==========

    @Override
    public Voucher save(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findById(Long id) {
        return voucherRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCode(code.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> findAll(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> search(String keyword, Pageable pageable) {
        return voucherRepository.searchVouchers(keyword, pageable);
    }

    @Override
    public void deleteById(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher", "id", id);
        }
        voucherRepository.deleteById(id);
        log.info("Deleted voucher:  {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return voucherRepository.existsByCode(code.toUpperCase());
    }

    // ========== CREATE & UPDATE ==========

    @Override
    public Voucher create(String code, String name, String description,
                          DiscountType discountType, BigDecimal discountValue,
                          BigDecimal maxDiscount, BigDecimal minOrderAmount,
                          Integer usageLimit, Integer usageLimitPerUser,
                          LocalDateTime startDate, LocalDateTime endDate, Boolean isActive) {

        String upperCode = code.toUpperCase();

        // Kiểm tra code trùng
        if (voucherRepository.existsByCode(upperCode)) {
            throw new BadRequestException("Mã voucher đã tồn tại");
        }

        // Validate
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Phần trăm giảm giá không được vượt quá 100%");
        }

        Voucher voucher = Voucher.builder()
                .code(upperCode)
                .name(name)
                .description(description)
                .discountType(discountType)
                .discountValue(discountValue)
                .maxDiscount(maxDiscount)
                .minOrderAmount(minOrderAmount)
                .usageLimit(usageLimit)
                .usageLimitPerUser(usageLimitPerUser != null ? usageLimitPerUser : 1)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(isActive != null ? isActive : true)
                .build();

        Voucher savedVoucher = voucherRepository.save(voucher);
        log.info("Created voucher: {}", upperCode);

        return savedVoucher;
    }

    @Override
    public Voucher update(Long id, String name, String description,
                          DiscountType discountType, BigDecimal discountValue,
                          BigDecimal maxDiscount, BigDecimal minOrderAmount,
                          Integer usageLimit, Integer usageLimitPerUser,
                          LocalDateTime startDate, LocalDateTime endDate, Boolean isActive) {

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));

        voucher.setName(name);
        voucher.setDescription(description);
        voucher.setDiscountType(discountType);
        voucher.setDiscountValue(discountValue);
        voucher.setMaxDiscount(maxDiscount);
        voucher.setMinOrderAmount(minOrderAmount);
        voucher.setUsageLimit(usageLimit);
        if (usageLimitPerUser != null) {
            voucher.setUsageLimitPerUser(usageLimitPerUser);
        }
        voucher.setStartDate(startDate);
        voucher.setEndDate(endDate);
        if (isActive != null) {
            voucher.setIsActive(isActive);
        }

        log.info("Updated voucher: {}", id);
        return voucherRepository.save(voucher);
    }

    @Override
    public void toggleActive(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));

        voucher.setIsActive(!voucher.getIsActive());
        voucherRepository.save(voucher);
        log.info("Toggled voucher active status: {} -> {}", id, voucher.getIsActive());
    }

    // ========== VALIDATION & APPLY ==========

    @Override
    @Transactional(readOnly = true)
    public boolean isVoucherValid(String code) {
        Optional<Voucher> voucherOpt = voucherRepository.findByCodeAndIsActiveTrue(code.toUpperCase());
        return voucherOpt.isPresent() && voucherOpt.get().isValid();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserUseVoucher(String code, Long userId) {
        Voucher voucher = voucherRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElse(null);

        if (voucher == null || !voucher.isValid()) {
            return false;
        }

        // Kiểm tra số lần user đã sử dụng
        int usedByUser = voucherUsageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        return usedByUser < voucher.getUsageLimitPerUser();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        Voucher voucher = voucherRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Mã voucher không hợp lệ"));

        if (!voucher.isValid()) {
            throw new BadRequestException("Mã voucher đã hết hạn hoặc hết lượt sử dụng");
        }

        return voucher.calculateDiscount(orderAmount);
    }

    @Override
    public Voucher applyVoucher(String code, Long userId, Long orderId) {
        Voucher voucher = voucherRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Mã voucher không hợp lệ"));

        if (!voucher.isValid()) {
            throw new BadRequestException("Mã voucher đã hết hạn hoặc hết lượt sử dụng");
        }

        // Kiểm tra user đã dùng voucher này chưa
        int usedByUser = voucherUsageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        if (usedByUser >= voucher.getUsageLimitPerUser()) {
            throw new BadRequestException("Bạn đã sử dụng hết lượt cho mã voucher này");
        }

        // Lấy user và order
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        }

        // Tạo voucher usage record
        VoucherUsage usage = VoucherUsage.create(voucher, user, order);
        voucherUsageRepository.save(usage);

        // Tăng số lần sử dụng
        voucher.incrementUsedCount();
        voucherRepository.save(voucher);

        log.info("Applied voucher {} for user {} on order {}", code, userId, orderId);
        return voucher;
    }

    // ========== FIND VOUCHERS ==========

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findValidVouchers() {
        return voucherRepository.findValidVouchers(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findApplicableVouchers(BigDecimal orderAmount) {
        return voucherRepository.findApplicableVouchers(orderAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findVouchersForUser(Long userId, BigDecimal orderAmount) {
        List<Voucher> applicableVouchers = voucherRepository.findApplicableVouchers(orderAmount);

        // Lọc vouchers mà user còn có thể sử dụng
        return applicableVouchers.stream()
                .filter(v -> {
                    int usedByUser = voucherUsageRepository.countByVoucherIdAndUserId(v.getId(), userId);
                    return usedByUser < v.getUsageLimitPerUser();
                })
                .collect(Collectors.toList());
    }

    // ========== MAINTENANCE ==========

    @Override
    public int deactivateExpiredVouchers() {
        int count = voucherRepository.deactivateExpiredVouchers();
        log.info("Deactivated {} expired vouchers", count);
        return count;
    }
}