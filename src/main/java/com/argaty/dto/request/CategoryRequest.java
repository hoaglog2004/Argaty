package com.argaty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @Size(max = 100, message = "Slug không được vượt quá 100 ký tự")
    private String slug;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    // QUAN TRỌNG: Xóa @NotBlank ở đây
    @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
    private String image;

    @Size(max = 50, message = "Icon không được vượt quá 50 ký tự")
    private String icon;

    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isFeatured;
    private Long parentId;
}