package com.argaty. repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data. jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Category;

/**
 * Repository cho Category Entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ========== FIND BY FIELD ==========

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    // ========== FIND ROOT CATEGORIES ==========

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true " +
           "ORDER BY c. displayOrder ASC")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.displayOrder ASC")
    List<Category> findAllRootCategories();

    // ========== FIND BY PARENT ==========

    List<Category> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);

    List<Category> findByParentIdOrderByDisplayOrderAsc(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent. id = :parentId")
    List<Category> findChildCategories(@Param("parentId") Long parentId);

    // ========== FIND FEATURED ==========

    @Query("SELECT c FROM Category c WHERE c.isFeatured = true AND c.isActive = true " +
           "ORDER BY c. displayOrder ASC")
    List<Category> findFeaturedCategories();

    // ========== FIND BY STATUS ==========

    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    Page<Category> findByIsActiveTrue(Pageable pageable);

    // ========== SEARCH ==========

    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> searchCategories(@Param("keyword") String keyword, Pageable pageable);

    // ========== WITH PRODUCT COUNT ==========

    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p " +
           "WHERE c.isActive = true AND c.parent IS NULL " +
           "GROUP BY c ORDER BY c.displayOrder ASC")
    List<Object[]> findRootCategoriesWithProductCount();

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND " +
           "EXISTS (SELECT p FROM Product p WHERE p.category = c AND p.isActive = true)")
    List<Category> findCategoriesWithActiveProducts();

    // ========== HIERARCHY QUERIES ==========

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL AND c.isActive = true " +
           "ORDER BY c. displayOrder ASC")
    List<Category> findRootCategoriesWithChildren();

    // ========== STATISTICS ==========

    @Query("SELECT COUNT(c) FROM Category c WHERE c.isActive = true")
    long countActiveCategories();

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent IS NULL")
    long countRootCategories();
}