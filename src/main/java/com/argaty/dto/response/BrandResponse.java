package com.argaty. dto.response;

import com. argaty.entity.Brand;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response thương hiệu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandResponse {

    private Long id;
    private String name;
    private String slug;
    private String logo;
    private String description;
    private String website;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer productCount;

    public static BrandResponse fromEntity(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logo(brand. getLogo())
                .description(brand.getDescription())
                .website(brand.getWebsite())
                .displayOrder(brand. getDisplayOrder())
                .isActive(brand.getIsActive())
                .productCount(brand. getProductCount())
                .build();
    }
}