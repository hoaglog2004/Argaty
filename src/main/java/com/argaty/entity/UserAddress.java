package com.argaty.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity UserAddress - Địa chỉ người dùng
 */
@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress extends BaseEntity {

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ========== HELPER METHODS ==========

    /**
     * Lấy địa chỉ đầy đủ
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(address);

        if (ward != null && !ward.isEmpty()) {
            sb.append(", ").append(ward);
        }
        sb.append(", ").append(district);
        sb.append(", ").append(city);

        return sb.toString();
    }
}