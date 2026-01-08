package com.argaty.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.argaty.entity.Brand;

/**
 * Service interface cho Brand
 */
public interface BrandService {

    Brand save(Brand brand);

    Optional<Brand> findById(Long id);

    Optional<Brand> findBySlug(String slug);

    List<Brand> findAllActive();

    Page<Brand> findAll(Pageable pageable);

    Page<Brand> search(String keyword, Pageable pageable);

    void deleteById(Long id);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    Brand create(String name, String description, String logo, String website);

    Brand update(Long id, String name, String description, String logo, String website);

    void toggleActive(Long id);

    List<Brand> findBrandsWithProducts();

    long countActiveBrands();
}