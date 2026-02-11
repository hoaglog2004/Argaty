package com.argaty.dto.request;

import java.util.List;

import com.argaty.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull; // Chỉ giữ lại Size, bỏ NotBlank
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    // --- BỎ @NotBlank Ở CÁC TRƯỜNG DƯỚI ĐÂY ---
    
    // @NotBlank(message = "Tên người nhận không được để trống") <- XÓA HOẶC COMMENT
    @Size(max = 100)
    private String receiverName;

    // @NotBlank... <- XÓA
    // @Pattern... <- XÓA (Sẽ check thủ công ở Controller)
    private String receiverPhone;

    private String receiverEmail; // Email optional sẵn rồi

    // @NotBlank... <- XÓA
    private String shippingAddress;

    // @NotBlank... <- XÓA
    private String city;

    // @NotBlank... <- XÓA
    private String district;

    private String ward;

    // --- CÁC TRƯỜNG KHÁC GIỮ NGUYÊN ---
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @Size(max = 50)
    private String voucherCode;

    @Size(max = 500)
    private String note;

    private Long addressId; // ID địa chỉ đã lưu
    private Boolean saveAddress;
    private List<Long> cartItemIds;
}