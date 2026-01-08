package com.argaty.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Voucher;

/**
 * Repository cho Voucher Entity
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    Optional<Voucher> findByCodeAndIsActiveTrue(String code);

    boolean existsByCode(String code);

    // ========== FIND VALID VOUCHERS ==========

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND " +
           "(v.startDate IS NULL OR v.startDate <= : now) AND " +
           "(v.endDate IS NULL OR v.endDate >= :now) AND " +
           "(v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND " +
           "(v.startDate IS NULL OR v.startDate <= CURRENT_TIMESTAMP) AND " +
           "(v.endDate IS NULL OR v.endDate >= CURRENT_TIMESTAMP) AND " +
           "(v.usageLimit IS NULL OR v.usedCount < v.usageLimit) AND " +
           "(v.minOrderAmount IS NULL OR v.minOrderAmount <= :orderAmount)")
    List<Voucher> findApplicableVouchers(@Param("orderAmount") java.math.BigDecimal orderAmount);

    // ========== SEARCH ==========

    @Query("SELECT v FROM Voucher v WHERE " +
           "LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Voucher> searchVouchers(@Param("keyword") String keyword, Pageable pageable);

    // ========== FIND BY STATUS ==========

    Page<Voucher> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE v.endDate < CURRENT_TIMESTAMP")
    List<Voucher> findExpiredVouchers();

    // ========== UPDATE ==========

    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 WHERE v.id = : voucherId")
    void incrementUsedCount(@Param("voucherId") Long voucherId);

    @Modifying
    @Query("UPDATE Voucher v SET v.isActive = false WHERE v.endDate < CURRENT_TIMESTAMP AND v.isActive = true")
    int deactivateExpiredVouchers();
}