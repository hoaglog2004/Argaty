package com.argaty.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.argaty.dto.request.ProductRequest;
import com.argaty.entity.Product;
import com.argaty.entity.ProductImage;
import com.argaty.entity.ProductVariant;
import com.argaty.entity.VariantImage;

/**
 * Service interface cho Product
 */
public interface ProductService {

    // ========== CRUD ==========

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySlugWithDetails(String slug);

    Optional<Product> findByIdWithDetails(Long id);

    Page<Product> findAll(Pageable pageable);

    void deleteById(Long id);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    // ========== FIND PRODUCTS ==========

    Page<Product> findActiveProducts(Pageable pageable);

    Page<Product> findByCategory(Long categoryId, Pageable pageable);

    Page<Product> findByBrand(Long brandId, Pageable pageable);

    Page<Product> findOnSale(Pageable pageable);

    List<Product> findFeaturedProducts(int limit);

    List<Product> findNewProducts(int limit);

    List<Product> findBestSellerProducts(int limit);

    List<Product> findRelatedProducts(Long productId, int limit);

    // ========== SEARCH & FILTER ==========

    Page<Product> search(String keyword, Pageable pageable);

    Page<Product> searchByCategory(String keyword, Long categoryId, Pageable pageable);

    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // [MỚI] Hàm lọc tổng hợp (Master Filter)
    Page<Product> filterProducts(String keyword, Long categoryId, Long brandId, 
                                 BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // ========== ADMIN METHODS (bao gồm cả inactive) ==========

    Page<Product> searchAll(String keyword, Pageable pageable);

    Page<Product> findAllByCategory(Long categoryId, Pageable pageable);

    Page<Product> findAllByBrand(Long brandId, Pageable pageable);

    // ========== CREATE & UPDATE ==========

    // [CẬP NHẬT] Thêm tham số SKU
    Product create(String name, String sku, String shortDescription, String description,
                   BigDecimal price, BigDecimal salePrice, Integer discountPercent,
                   Integer quantity, Long categoryId, Long brandId,
                   Boolean isFeatured, Boolean isNew, Boolean isBestSeller,
                   String specifications, String metaTitle, String metaDescription,
                   java.time.LocalDateTime saleStartDate, java.time.LocalDateTime saleEndDate,
                   String tier1Name, String tier2Name);

    // [CẬP NHẬT] Đổi tham số thành DTO ProductRequest
    Product update(Long id, ProductRequest request);

    void toggleActive(Long id);

    void toggleFeatured(Long id);

    void toggleNew(Long id);

    // ========== IMAGES ==========

    ProductImage addImage(Long productId, String imageUrl, boolean isMain);

    void removeImage(Long imageId);

    void setMainImage(Long productId, Long imageId);

    // ========== VARIANTS ==========

    ProductVariant addVariant(Long productId, String name, String sku, String color, String colorCode,
                              String size, BigDecimal additionalPrice, Integer quantity);

    // Variant images
    VariantImage addVariantImage(Long variantId, String imageUrl, boolean isMain);

    ProductVariant updateVariant(Long variantId, String name, String sku, String color, String colorCode,
                                 String size, BigDecimal additionalPrice, Integer quantity);

    void removeVariant(Long variantId);

    // ========== STOCK ==========

    void decreaseStock(Long productId, Long variantId, int quantity);

    void increaseStock(Long productId, Long variantId, int quantity);

    List<Product> findLowStockProducts();

    List<Product> findOutOfStockProducts();

    // ========== RATING ==========

    void updateRating(Long productId);

    // ========== STATISTICS ==========

    long countActiveProducts();

    long countOutOfStockProducts();

    Long getTotalStock();
}