package com.argaty.service;

import java.math.BigDecimal;
import java.util.List;
import java. util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain. Pageable;

import com.argaty.entity.Product;
import com.argaty.entity.ProductImage;
import com.argaty.entity.ProductVariant;

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

    // ========== CREATE & UPDATE ==========

    Product create(String name, String shortDescription, String description,
                   BigDecimal price, BigDecimal salePrice, Integer discountPercent,
                   Integer quantity, Long categoryId, Long brandId,
                   Boolean isFeatured, Boolean isNew);

    Product update(Long id, String name, String shortDescription, String description,
                   BigDecimal price, BigDecimal salePrice, Integer discountPercent,
                   Integer quantity, Long categoryId, Long brandId,
                   Boolean isFeatured, Boolean isNew);

    void toggleActive(Long id);

    void toggleFeatured(Long id);

    // ========== IMAGES ==========

    ProductImage addImage(Long productId, String imageUrl, boolean isMain);

    void removeImage(Long imageId);

    void setMainImage(Long productId, Long imageId);

    // ========== VARIANTS ==========

    ProductVariant addVariant(Long productId, String name, String color, String colorCode,
                              String size, BigDecimal additionalPrice, Integer quantity);

    ProductVariant updateVariant(Long variantId, String name, String color, String colorCode,
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