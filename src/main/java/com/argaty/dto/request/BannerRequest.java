package com.argaty.dto.request;

import jakarta.validation.constraints.Size; // Bỏ import NotBlank nếu không dùng ở đâu khác
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho yêu cầu thêm/cập nhật banner
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerRequest {

    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @Size(max = 300, message = "Tiêu đề phụ không được vượt quá 300 ký tự")
    private String subtitle;

    // --- SỬA Ở ĐÂY: Xóa bỏ @NotBlank ---
    // Vì khi tạo mới, chúng ta upload file ảnh để lấy URL sau, 
    // nên lúc submit form thì trường này được phép null.
    // Việc kiểm tra "phải có ảnh" đã được xử lý trong Controller (kiểm tra MultipartFile).
    
    @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
    private String imageUrl; 

    @Size(max = 500, message = "Đường dẫn không được vượt quá 500 ký tự")
    private String link;

    @Size(max = 30, message = "Vị trí không được vượt quá 30 ký tự")
    private String position;

    private Integer displayOrder;

    private Boolean isActive;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}