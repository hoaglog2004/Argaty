package com. argaty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation. constraints.Size;
import lombok. AllArgsConstructor;
import lombok.Builder;
import lombok. Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request phản hồi đánh giá (Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Size(max = 1000, message = "Nội dung phản hồi tối đa 1000 ký tự")
    private String reply;
}