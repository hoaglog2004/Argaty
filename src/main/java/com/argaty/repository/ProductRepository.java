package com.argaty.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Product;

/**
 * Repository cho Product Entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ========== FIND BY FIELD ==========

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlugAndIsActiveTrue(String slug);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.variants " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.slug = :slug AND p.isActive = true")
    Optional<Product> findBySlugWithAllDetails(@Param("slug") String slug);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    // ========== FIND BY CATEGORY ==========

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId OR p.category.parent.id = :categoryId")
    Page<Product> findByCategoryAndSubcategories(@Param("categoryId") Long categoryId, Pageable pageable);

    List<Product> findTop10ByCategoryIdAndIsActiveTrueAndIdNot(Long categoryId, Long excludeId);

    // ========== FIND BY BRAND ==========

    Page<Product> findByBrandIdAndIsActiveTrue(Long brandId, Pageable pageable);

    // Admin: lấy tất cả sản phẩm theo brand (kể cả inactive)
    Page<Product> findByBrandId(Long brandId, Pageable pageable);

    // Admin: lấy tất cả sản phẩm theo category (kể cả inactive)
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId OR p.category.parent.id = :categoryId")
    Page<Product> findAllByCategoryAndSubcategories(@Param("categoryId") Long categoryId, Pageable pageable);

    // ========== FIND FEATURED / NEW / BESTSELLER ==========

    @Query("SELECT p FROM Product p WHERE p.isFeatured = true AND p.isActive = true " +
           "ORDER BY p.createdAt DESC")
    List<Product> findFeaturedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isNew = true AND p.isActive = true " +
           "ORDER BY p.createdAt DESC")
    List<Product> findNewProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isBestSeller = true AND p.isActive = true " +
           "ORDER BY p.soldCount DESC")
    List<Product> findBestSellerProducts(Pageable pageable);

    // ========== FIND ACTIVE PRODUCTS ==========
    
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Product> findByIsActiveTrueOrderBySoldCountDesc(Pageable pageable);

    // ========== FIND ON SALE ==========

    @Query("SELECT p FROM Product p WHERE p.salePrice IS NOT NULL AND p.salePrice < p.price " +
           "AND p.isActive = true AND " +
           "(p.saleStartDate IS NULL OR p.saleStartDate <= CURRENT_TIMESTAMP) AND " +
           "(p.saleEndDate IS NULL OR p.saleEndDate >= CURRENT_TIMESTAMP) " +
           "ORDER BY p.discountPercent DESC")
    Page<Product> findOnSaleProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.salePrice IS NOT NULL AND p.salePrice < p.price " +
           "AND p.isActive = true ORDER BY p.discountPercent DESC")
    List<Product> findTopSaleProducts(Pageable pageable);

    // ========== SEARCH ==========

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    // Admin: tìm kiếm tất cả sản phẩm (kể cả inactive)
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchAllProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "p.category.id = :categoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProductsByCategory(@Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId,
                                           Pageable pageable);

    // ========== FILTER BY PRICE ==========

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "COALESCE(p.salePrice, p.price) BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    // ========== FIND BY STOCK STATUS ==========

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.lowStockThreshold AND p.quantity > 0 " +
           "AND p.isActive = true")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.quantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();

    // ========== RELATED PRODUCTS ==========

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id <> :productId " +
           "AND p.isActive = true ORDER BY p.soldCount DESC")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId,
                                      @Param("productId") Long productId,
                                      Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.id <> :productId " +
           "AND p.isActive = true ORDER BY p.soldCount DESC")
    List<Product> findRelatedByBrand(@Param("brandId") Long brandId,
                                     @Param("productId") Long productId,
                                     Pageable pageable);

    // ========== UPDATE ==========

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :quantity, " +
           "p.soldCount = p.soldCount + :quantity WHERE p.id = :productId AND p.quantity >= :quantity")
    int decreaseQuantity(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :quantity WHERE p.id = :productId")
    void increaseQuantity(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.rating = :rating, p.reviewCount = :reviewCount WHERE p.id = :productId")
    void updateRating(@Param("productId") Long productId,
                      @Param("rating") BigDecimal rating,
                      @Param("reviewCount") int reviewCount);

    // ========== STATISTICS ==========

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity = 0")
    long countOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity <= p.lowStockThreshold AND p.quantity > 0")
    long countLowStockProducts();

    @Query("SELECT SUM(p.quantity) FROM Product p WHERE p.isActive = true")
    Long getTotalStock();

    @Query("SELECT SUM(p.soldCount) FROM Product p")
    Long getTotalSoldCount();

    // ========== ADMIN QUERIES ==========

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand")
    Page<Product> findAllWithCategoryAndBrand(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.variants " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithAllDetails(@Param("id") Long id);
}