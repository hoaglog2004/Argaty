package com.argaty.dto. request;

import jakarta.validation. constraints. NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request áp dụng voucher
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;
}