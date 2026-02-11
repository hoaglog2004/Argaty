package com.argaty.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    
    private Long id;              // Để nhận biết biến thể cũ cần update
    
    private String name;          // Tên biến thể (VD: Đen - L)
    private String sku;           // Mã SKU riêng
    
    private String color;         // Tên màu
    private String colorCode;     // Mã màu Hex (#000000)
    
    private String size;          // Kích thước
    
    private BigDecimal additionalPrice; // Giá cộng thêm
    private Integer quantity;           // Số lượng tồn kho
    
    // List URL ảnh của biến thể (nếu có upload ảnh riêng cho biến thể)
    private List<String> imageUrls; 
}