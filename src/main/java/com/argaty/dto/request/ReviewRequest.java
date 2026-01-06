package com.argaty.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho yêu cầu thêm/cập nhật đánh giá sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    private Long orderItemId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1 đến 5")
    private Integer rating;

    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private String comment;

    private List<String> imageUrls;
}
