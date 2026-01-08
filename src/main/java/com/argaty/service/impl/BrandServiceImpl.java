package com.argaty.service.impl;

import com.argaty.entity.Brand;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.exception.BadRequestException;
import com.argaty.repository.BrandRepository;
import com.argaty.service.BrandService;
import com.argaty.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation của BrandService
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

    @Override
    @Transactional(readOnly = true)
    public Page<Brand> search(String keyword, Pageable pageable) {
        return brandRepository.searchBrands(keyword, pageable);
    }

    @Override
    public void deleteById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Kiểm tra có sản phẩm không
        if (brand.getProductCount() > 0) {
            throw new BadRequestException("Không thể xóa thương hiệu đang có sản phẩm");
        }

        brandRepository.deleteById(id);
        log.info("Deleted brand: {}", id);
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
    public Brand create(String name, String description, String logo, String website) {
        // Kiểm tra tên trùng
        if (brandRepository.existsByName(name)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        // Tạo slug
        String slug = SlugUtil.toSlug(name);
        int count = 1;
        String originalSlug = slug;
        while (brandRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + count++;
        }

        Brand brand = Brand.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .logo(logo)
                .website(website)
                .isActive(true)
                .build();

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand: {}", name);

        return savedBrand;
    }

    @Override
    public Brand update(Long id, String name, String description, String logo, String website) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Kiểm tra tên trùng với brand khác
        if (!brand.getName().equals(name) && brandRepository.existsByName(name)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        // Cập nhật slug nếu tên thay đổi
        if (!brand.getName().equals(name)) {
            String slug = SlugUtil.toSlug(name);
            int count = 1;
            String originalSlug = slug;
            while (brandRepository.existsBySlug(slug) && !slug.equals(brand.getSlug())) {
                slug = originalSlug + "-" + count++;
            }
            brand.setSlug(slug);
        }

        brand.setName(name);
        brand.setDescription(description);
        if (logo != null) {
            brand.setLogo(logo);
        }
        brand.setWebsite(website);

        log.info("Updated brand: {}", id);
        return brandRepository.save(brand);
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
    @Transactional(readOnly = true)
    public List<Brand> findBrandsWithProducts() {
        return brandRepository.findBrandsWithActiveProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveBrands() {
        return brandRepository.countActiveBrands();
    }
}