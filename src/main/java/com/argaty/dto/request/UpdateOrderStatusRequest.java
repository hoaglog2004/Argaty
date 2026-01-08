package com.argaty.dto.request;

import com.argaty.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request cập nhật trạng thái đơn hàng (Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private OrderStatus status;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}