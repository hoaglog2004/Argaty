package com.argaty.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.argaty.entity.Brand;

public interface BrandService {
    
    // Các hàm Query cơ bản
    Brand save(Brand brand);
    Optional<Brand> findById(Long id);
    Optional<Brand> findBySlug(String slug);
    List<Brand> findAllActive();
    Page<Brand> findAll(Pageable pageable);
    Page<Brand> search(String keyword, Pageable pageable);
    void deleteById(Long id);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    List<Brand> findBrandsWithProducts();
    long countActiveBrands();
    void toggleActive(Long id);

    // --- CẬP NHẬT 2 HÀM NÀY ---
    // Thêm tham số slug và isActive vào để khớp với Controller
    Brand create(String name, String slug, String logo, String description, Boolean isActive);

    Brand update(Long id, String name, String slug, String logo, String description, Boolean isActive);
}