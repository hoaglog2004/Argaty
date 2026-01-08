package com.argaty.dto.response;

import com.argaty.entity.Banner;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response banner
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerResponse {

    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String link;
    private String position;
    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isCurrentlyActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    public static BannerResponse fromEntity(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .link(banner.getLink())
                .position(banner.getPosition())
                .displayOrder(banner.getDisplayOrder())
                .isActive(banner.getIsActive())
                .isCurrentlyActive(banner.isCurrentlyActive())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .createdAt(banner.getCreatedAt())
                .build();
    }
}