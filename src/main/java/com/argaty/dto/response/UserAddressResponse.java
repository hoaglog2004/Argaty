package com.argaty.dto.response;

import com.argaty.entity.UserAddress;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response địa chỉ người dùng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAddressResponse {

    private Long id;
    private String receiverName;
    private String phone;
    private String address;
    private String city;
    private String district;
    private String ward;
    private String fullAddress;
    private Boolean isDefault;

    public static UserAddressResponse fromEntity(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .address(address.getAddress())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .fullAddress(address.getFullAddress())
                .isDefault(address.getIsDefault())
                .build();
    }
}