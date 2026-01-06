package com.argaty.service;

import com.argaty.entity.UserAddress;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho UserAddress
 */
public interface UserAddressService {

    UserAddress save(UserAddress address);

    Optional<UserAddress> findById(Long id);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    List<UserAddress> findByUserId(Long userId);

    Optional<UserAddress> findDefaultAddress(Long userId);

    void deleteById(Long id, Long userId);

    UserAddress create(Long userId, String receiverName, String phone,
                       String address, String city, String district,
                       String ward, boolean isDefault);

    UserAddress update(Long id, Long userId, String receiverName, String phone,
                       String address, String city, String district,
                       String ward, boolean isDefault);

    void setDefaultAddress(Long userId, Long addressId);

    int countByUserId(Long userId);
}