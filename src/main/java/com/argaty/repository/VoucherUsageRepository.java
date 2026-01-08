package com.argaty.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.VoucherUsage;

/**
 * Repository cho VoucherUsage Entity
 */
@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    List<VoucherUsage> findByUserId(Long userId);

    List<VoucherUsage> findByVoucherId(Long voucherId);

    @Query("SELECT COUNT(vu) FROM VoucherUsage vu WHERE vu.voucher.id = :voucherId AND vu.user.id = :userId")
    int countByVoucherIdAndUserId(@Param("voucherId") Long voucherId, @Param("userId") Long userId);

    boolean existsByVoucherIdAndUserId(Long voucherId, Long userId);

    @Query("SELECT COUNT(vu) FROM VoucherUsage vu WHERE vu.voucher.id = :voucherId")
    long countByVoucherId(@Param("voucherId") Long voucherId);
}