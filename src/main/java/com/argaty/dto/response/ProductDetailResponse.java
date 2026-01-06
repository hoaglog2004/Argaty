package com. argaty.dto.response;

import com.argaty.entity. Product;
import com.argaty.entity.ProductImage;
import com.argaty. entity.ProductVariant;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java. util.List;
import java.util.stream.Collectors;

/**
 * DTO cho response chi tiết sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private Integer quantity;
    private Integer soldCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean isNew;
    private Boolean isFeatured;
    private Boolean isBestSeller;
    private Boolean isOnSale;
    private Boolean isInStock;
    private Boolean isLowStock;
    private String specifications;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;
    private LocalDateTime createdAt;

    // Category
    private CategoryResponse category;

    // Brand
    private BrandResponse brand;

    // Images
    private List<ImageResponse> images;

    // Variants
    private List<VariantResponse> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isMain;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantResponse {
        private Long id;
        private String name;
        private String sku;
        private String color;
        private String colorCode;
        private String size;
        private BigDecimal additionalPrice;
        private BigDecimal finalPrice;
        private Integer quantity;
        private Boolean isActive;
        private Boolean isInStock;
        private List<String> images;
    }

    public static ProductDetailResponse fromEntity(Product product) {
        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .discountPercent(product.getCalculatedDiscountPercent())
                .quantity(product.getQuantity())
                .soldCount(product. getSoldCount())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isNew(product.getIsNew())
                .isFeatured(product.getIsFeatured())
                .isBestSeller(product.getIsBestSeller())
                .isOnSale(product.isOnSale())
                .isInStock(product.isInStock())
                .isLowStock(product.isLowStock())
                .specifications(product.getSpecifications())
                .saleStartDate(product.getSaleStartDate())
                .saleEndDate(product. getSaleEndDate())
                .createdAt(product.getCreatedAt())
                .build();

        // Category
        if (product.getCategory() != null) {
            response.setCategory(CategoryResponse.fromEntity(product.getCategory()));
        }

        // Brand
        if (product.getBrand() != null) {
            response.setBrand(BrandResponse.fromEntity(product.getBrand()));
        }

        // Images
        if (product. getImages() != null) {
            response.setImages(product.getImages().stream()
                    .map(img -> ImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isMain(img.getIsMain())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Variants
        if (product. getVariants() != null) {
            response.setVariants(product.getVariants().stream()
                    .filter(ProductVariant::getIsActive)
                    .map(v -> VariantResponse.builder()
                            .id(v. getId())
                            .name(v.getName())
                            . sku(v.getSku())
                            .color(v. getColor())
                            .colorCode(v.getColorCode())
                            .size(v. getSize())
                            .additionalPrice(v.getAdditionalPrice())
                            .finalPrice(v.getFinalPrice())
                            .quantity(v. getQuantity())
                            . isActive(v.getIsActive())
                            .isInStock(v.isInStock())
                            .images(v.getImages() != null ? 
                                    v.getImages().stream()
                                            . map(img -> img.getImageUrl())
                                            .collect(Collectors.toList()) : null)
                            .build())
                    .collect(Collectors.toList()));
        }

        return response;
    }
}