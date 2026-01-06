package com.argaty. dto.response;

import com.argaty.entity.User;
import com.argaty.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response thông tin user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String address;
    private String city;
    private String district;
    private String ward;
    private Role role;
    private Boolean isEnabled;
    private Boolean isBanned;
    private String banReason;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    // Thống kê (cho admin)
    private Long orderCount;
    private Long totalSpent;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .address(user. getAddress())
                .city(user.getCity())
                .district(user.getDistrict())
                .ward(user. getWard())
                .role(user.getRole())
                .isEnabled(user.getIsEnabled())
                .isBanned(user.getIsBanned())
                .banReason(user.getBanReason())
                .emailVerifiedAt(user. getEmailVerifiedAt())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserResponse basicInfo(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
}