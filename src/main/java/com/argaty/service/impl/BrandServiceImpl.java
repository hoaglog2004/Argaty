package com.argaty.service.impl;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.argaty.entity.Brand;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.BrandRepository;
import com.argaty.service.BrandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation của BrandService (Đã fix lỗi đỏ)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findById(Long id) {
        return brandRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findBySlug(String slug) {
        return brandRepository.findBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findAllActive() {
        return brandRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Brand> findAll(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    // FIX DÒNG 66
    @Override
    @Transactional(readOnly = true)
    public Page<Brand> search(String keyword, Pageable pageable) {
        // Đã khai báo hàm này trong Repository ở Bước 1
        return brandRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    // FIX DÒNG 76
    @Override
    public void deleteById(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand", "id", id);
        }

        // Đã xóa đoạn check productCount vì Entity không có.
        // Nếu Brand đang có sản phẩm, DB sẽ ném DataIntegrityViolationException.
        // Controller sẽ bắt Exception chung và thông báo lỗi.
        
        try {
            brandRepository.deleteById(id);
            log.info("Deleted brand: {}", id);
        } catch (Exception e) {
            // Ném lỗi rõ ràng hơn để Controller hiển thị
            throw new BadRequestException("Không thể xóa thương hiệu này (có thể đang chứa sản phẩm)");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return brandRepository.existsBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return brandRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findBrandsWithProducts() {
        return brandRepository.findBrandsWithActiveProducts();
    }

    // FIX DÒNG 106
    @Override
    @Transactional(readOnly = true)
    public long countActiveBrands() {
        // Đã khai báo hàm này trong Repository ở Bước 1
        return brandRepository.countByIsActiveTrue();
    }

    @Override
    public void toggleActive(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brand.setIsActive(!brand.getIsActive());
        brandRepository.save(brand);
        log.info("Toggled brand active status: {} -> {}", id, brand.getIsActive());
    }

    @Override
    public Brand create(String name, String slug, String logo, String description, Boolean isActive) {
        if (brandRepository.existsByName(name)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại: " + name);
        }

        if (!StringUtils.hasText(slug)) {
            slug = toSlug(name);
        }
        if (brandRepository.existsBySlug(slug)) {
            int count = 1;
            String originalSlug = slug;
            while (brandRepository.existsBySlug(slug)) {
                slug = originalSlug + "-" + count++;
            }
        }

        Brand brand = new Brand();
        brand.setName(name);
        brand.setSlug(slug);
        brand.setLogo(logo);
        brand.setDescription(description);
        brand.setIsActive(isActive != null ? isActive : true);
        brand.setDisplayOrder(0); 

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand: {}", name);
        return savedBrand;
    }

    @Override
    public Brand update(Long id, String name, String slug, String logo, String description, Boolean isActive) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        if (!brand.getName().equals(name) && brandRepository.existsByName(name)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        if (!StringUtils.hasText(slug)) {
            slug = toSlug(name);
        }
        if (!brand.getSlug().equals(slug) && brandRepository.existsBySlug(slug)) {
            throw new BadRequestException("Slug đã tồn tại");
        }

        brand.setName(name);
        brand.setSlug(slug);
        brand.setDescription(description);
        brand.setIsActive(isActive != null ? isActive : true);

        if (logo != null && !logo.isEmpty()) {
            brand.setLogo(logo);
        }

        log.info("Updated brand: {}", id);
        return brandRepository.save(brand);
    }

    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }
}