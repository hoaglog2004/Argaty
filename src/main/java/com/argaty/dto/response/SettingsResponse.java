package com.argaty.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for system settings response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {

    // General settings
    private String storeName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String seoDescription;

    // Shipping settings
    private BigDecimal defaultShippingFee;
    private BigDecimal freeShippingThreshold;
    private Integer estimatedDeliveryDays;

    // Payment settings
    private Boolean codEnabled;
    private Boolean onlinePaymentEnabled;

    // Email settings
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private Boolean smtpTls;
}
