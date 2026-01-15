package com.argaty.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request tạo/cập nhật sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    private String name;

    private String slug;

    private String sku;

    @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự")
    private String shortDescription;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0", message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @DecimalMin(value = "0", message = "Giá sale phải lớn hơn hoặc bằng 0")
    private BigDecimal salePrice;

    @Min(value = 0, message = "Phần trăm giảm giá phải từ 0-100")
    @Max(value = 100, message = "Phần trăm giảm giá phải từ 0-100")
    private Integer discountPercent;

    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private Long brandId;

    private Boolean isFeatured;

    private Boolean isNew;

    private Boolean isBestSeller;

    private Boolean isActive;

    private LocalDateTime saleStartDate;

    private LocalDateTime saleEndDate;

    private String specifications;

    private String metaTitle;

    private String metaDescription;

    // Danh sách ảnh
    private List<String> imageUrls;

    // Danh sách variants
    private List<ProductVariantRequest> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantRequest {
        
        @NotBlank(message = "Tên phân loại không được để trống")
        private String name;
        
        private String sku;
        
        private String color;
        
        private String colorCode;
        
        private String size;
        
        private BigDecimal additionalPrice;
        
        private Integer quantity;
        
        private List<String> imageUrls;
    }
}