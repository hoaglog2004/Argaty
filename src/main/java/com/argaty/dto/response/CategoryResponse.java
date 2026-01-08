package com.argaty.dto.response;

import com.argaty.entity.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO cho response danh mục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private String icon;
    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isFeatured;
    private Integer productCount;

    // Parent category
    private Long parentId;
    private String parentName;

    // Children categories
    private List<CategoryResponse> children;

    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse response = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .image(category.getImage())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .isFeatured(category.getIsFeatured())
                .productCount(category.getProductCount())
                .build();

        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }

        return response;
    }

    public static CategoryResponse fromEntityWithChildren(Category category) {
        CategoryResponse response = fromEntity(category);

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            response.setChildren(category.getChildren().stream()
                    .filter(Category::getIsActive)
                    .map(CategoryResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return response;
    }
}