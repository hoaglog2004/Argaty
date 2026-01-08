package com.argaty.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity Banner - Banner quảng cáo
 */
@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner extends BaseEntity {

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "position", nullable = false, length = 30)
    @Builder.Default
    private String position = "HOME_SLIDER";

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // ========== CONSTANTS ==========

    public static final String POSITION_HOME_SLIDER = "HOME_SLIDER";
    public static final String POSITION_HOME_BANNER = "HOME_BANNER";
    public static final String POSITION_PRODUCT_BANNER = "PRODUCT_BANNER";
    public static final String POSITION_POPUP = "POPUP";

    // ========== HELPER METHODS ==========

    /**
     * Kiểm tra banner đang trong thời gian hiển thị
     */
    public boolean isCurrentlyActive() {
        if (!isActive) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra thời gian bắt đầu
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }

        // Kiểm tra thời gian kết thúc
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra banner đã hết hạn
     */
    public boolean isExpired() {
        if (endDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * Kiểm tra banner chưa bắt đầu
     */
    public boolean isScheduled() {
        if (startDate == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(startDate);
    }
}