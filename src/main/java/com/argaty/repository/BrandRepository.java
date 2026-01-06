package com. argaty.repository;

import java.util.List;
import java. util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework. data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query. Param;
import org.springframework.stereotype. Repository;

import com.argaty.entity. Brand;

/**
 * Repository cho Brand Entity
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    // ========== FIND BY FIELD ==========

    Optional<Brand> findBySlug(String slug);

    Optional<Brand> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    // ========== FIND BY STATUS ==========

    List<Brand> findByIsActiveTrueOrderByDisplayOrderAsc();

    Page<Brand> findByIsActiveTrue(Pageable pageable);

    // ========== SEARCH ==========

    @Query("SELECT b FROM Brand b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Brand> searchBrands(@Param("keyword") String keyword, Pageable pageable);

    // ========== WITH PRODUCT COUNT ==========

    @Query("SELECT b, COUNT(p) FROM Brand b LEFT JOIN b.products p " +
           "WHERE b.isActive = true GROUP BY b ORDER BY b.displayOrder ASC")
    List<Object[]> findAllWithProductCount();

    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND " +
           "EXISTS (SELECT p FROM Product p WHERE p.brand = b AND p.isActive = true)")
    List<Brand> findBrandsWithActiveProducts();

    // ========== STATISTICS ==========

    @Query("SELECT COUNT(b) FROM Brand b WHERE b.isActive = true")
    long countActiveBrands();
}