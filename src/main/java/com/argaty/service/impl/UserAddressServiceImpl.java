package com.argaty.service.impl;

import com.argaty. entity.User;
import com.argaty. entity.UserAddress;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty. exception.BadRequestException;
import com.argaty.repository.UserAddressRepository;
import com.argaty.repository.UserRepository;
import com.argaty.service. UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation của UserAddressService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    private static final int MAX_ADDRESSES_PER_USER = 10;

    @Override
    public UserAddress save(UserAddress address) {
        return userAddressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAddress> findById(Long id) {
        return userAddressRepository. findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAddress> findByIdAndUserId(Long id, Long userId) {
        return userAddressRepository.findByIdAndUserId(id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAddress> findByUserId(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAddress> findDefaultAddress(Long userId) {
        return userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    public void deleteById(Long id, Long userId) {
        UserAddress address = userAddressRepository. findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress", "id", id));

        boolean wasDefault = address.getIsDefault();
        userAddressRepository.delete(address);

        // Nếu xóa địa chỉ mặc định, set địa chỉ khác làm mặc định
        if (wasDefault) {
            List<UserAddress> remainingAddresses = userAddressRepository
                    .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remainingAddresses.isEmpty()) {
                UserAddress newDefault = remainingAddresses.get(0);
                newDefault. setIsDefault(true);
                userAddressRepository. save(newDefault);
            }
        }

        log.info("Deleted address {} for user {}", id, userId);
    }

    @Override
    public UserAddress create(Long userId, String receiverName, String phone,
                              String address, String city, String district,
                              String ward, boolean isDefault) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Kiểm tra số lượng địa chỉ
        int addressCount = userAddressRepository. countByUserId(userId);
        if (addressCount >= MAX_ADDRESSES_PER_USER) {
            throw new BadRequestException("Bạn chỉ có thể lưu tối đa " + MAX_ADDRESSES_PER_USER + " địa chỉ");
        }

        // Nếu là địa chỉ đầu tiên hoặc set mặc định, clear các địa chỉ mặc định khác
        if (isDefault || addressCount == 0) {
            userAddressRepository.clearDefaultAddress(userId);
            isDefault = true;
        }

        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .receiverName(receiverName)
                .phone(phone)
                .address(address)
                .city(city)
                .district(district)
                .ward(ward)
                .isDefault(isDefault)
                .build();

        log.info("Created address for user {}", userId);
        return userAddressRepository.save(userAddress);
    }

    @Override
    public UserAddress update(Long id, Long userId, String receiverName, String phone,
                              String address, String city, String district,
                              String ward, boolean isDefault) {

        UserAddress userAddress = userAddressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress", "id", id));

        userAddress.setReceiverName(receiverName);
        userAddress.setPhone(phone);
        userAddress.setAddress(address);
        userAddress.setCity(city);
        userAddress.setDistrict(district);
        userAddress.setWard(ward);

        // Xử lý địa chỉ mặc định
        if (isDefault && !userAddress.getIsDefault()) {
            userAddressRepository.clearDefaultAddress(userId);
            userAddress.setIsDefault(true);
        }

        log.info("Updated address {} for user {}", id, userId);
        return userAddressRepository. save(userAddress);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress", "id", addressId));

        userAddressRepository.clearDefaultAddress(userId);
        userAddressRepository.setDefaultAddress(addressId);

        log.info("Set default address {} for user {}", addressId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByUserId(Long userId) {
        return userAddressRepository.countByUserId(userId);
    }
}