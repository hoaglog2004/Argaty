package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data. jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data. jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.UserAddress;

/**
 * Repository cho UserAddress Entity
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    int countByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user.id = : userId")
    void clearDefaultAddress(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = true WHERE ua.id = :addressId")
    void setDefaultAddress(@Param("addressId") Long addressId);

    @Modifying
    @Query("DELETE FROM UserAddress ua WHERE ua.id = :addressId AND ua.user.id = :userId")
    void deleteByIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);
}