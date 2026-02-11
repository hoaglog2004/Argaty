package com.argaty.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu thêm/cập nhật thương hiệu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequest {

    // @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 100, message = "Tên thương hiệu không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 100, message = "Slug không được vượt quá 100 ký tự")
    private String slug;

    @Size(max = 500, message = "URL logo không được vượt quá 500 ký tự")
    private String logo;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;

    private Boolean isActive;

    private Integer displayOrder;
}
