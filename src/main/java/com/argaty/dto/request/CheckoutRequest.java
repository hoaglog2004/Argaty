package com.argaty.dto.request;

import com.argaty.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho yêu cầu thanh toán đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    // Thông tin người nhận
    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận không được vượt quá 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String receiverPhone;

    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String receiverEmail;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String shippingAddress;

    @NotBlank(message = "Thành phố không được để trống")
    @Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    private String city;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    private String district;

    @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
    private String ward;

    // Phương thức thanh toán
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    // Mã giảm giá
    @Size(max = 50, message = "Mã giảm giá không được vượt quá 50 ký tự")
    private String voucherCode;

    // Ghi chú
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;

    // Sử dụng địa chỉ đã lưu
    private Long addressId;

    // Lưu địa chỉ mới
    private Boolean saveAddress;

    // Danh sách ID các sản phẩm trong giỏ hàng cần thanh toán
    private List<Long> cartItemIds;
}
