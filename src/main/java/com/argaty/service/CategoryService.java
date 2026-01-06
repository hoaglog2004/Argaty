package com.argaty. service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.argaty.entity.Category;

/**
 * Service interface cho Category
 */
public interface CategoryService {

    Category save(Category category);

    Optional<Category> findById(Long id);

    Optional<Category> findBySlug(String slug);

    List<Category> findAllActive();

    List<Category> findRootCategories();

    List<Category> findFeaturedCategories();

    List<Category> findChildCategories(Long parentId);

    Page<Category> findAll(Pageable pageable);

    Page<Category> search(String keyword, Pageable pageable);

    void deleteById(Long id);

    boolean existsBySlug(String slug);

    Category create(String name, String description, String image, String icon, Long parentId);

    Category update(Long id, String name, String description, String image, String icon, Long parentId);

    void toggleActive(Long id);

    void toggleFeatured(Long id);

    List<Category> findCategoriesWithProducts();

    long countActiveCategories();
}