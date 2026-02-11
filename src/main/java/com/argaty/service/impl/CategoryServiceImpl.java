package com.argaty.service.impl;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.argaty.entity.Category;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.CategoryRepository;
import com.argaty.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllActive() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findFeaturedCategories() {
        List<Category> categories = categoryRepository.findFeaturedCategories();
        if (categories.isEmpty()) {
            return categoryRepository.findRootCategories();
        }
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findChildCategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> search(String keyword, Pageable pageable) {
        return categoryRepository.searchCategories(keyword, pageable);
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.hasChildren()) {
            throw new BadRequestException("Không thể xóa danh mục đang có danh mục con");
        }

        if (category.getProductCount() > 0) {
            throw new BadRequestException("Không thể xóa danh mục đang có sản phẩm");
        }

        categoryRepository.deleteById(id);
        log.info("Deleted category: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    public Category create(String name, String description, String image, String icon, Long parentId) {
        // Tạo slug
        String slug = toSlug(name);
        int count = 1;
        String originalSlug = slug;
        while (categoryRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + count++;
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .image(image)
                .icon(icon)
                .isActive(true)
                .isFeatured(false)
                .build();

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category: {}", name);
        return savedCategory;
    }

    @Override
    public Category update(Long id, String name, String description, String image, String icon, Long parentId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Cập nhật slug nếu tên thay đổi
        if (!category.getName().equals(name)) {
            String slug = toSlug(name);
            int count = 1;
            String originalSlug = slug;
            while (categoryRepository.existsBySlug(slug) && !slug.equals(category.getSlug())) {
                slug = originalSlug + "-" + count++;
            }
            category.setSlug(slug);
        }

        category.setName(name);
        category.setDescription(description);
        
        // Chỉ cập nhật ảnh nếu có ảnh mới
        if (image != null && !image.isEmpty()) {
            category.setImage(image);
        }
        
        category.setIcon(icon);

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new BadRequestException("Không thể chọn danh mục cha là chính nó");
            }
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        log.info("Updated category: {}", id);
        return categoryRepository.save(category);
    }

    @Override
    public void toggleActive(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }

    @Override
    public void toggleFeatured(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setIsFeatured(!category.getIsFeatured());
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithActiveProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveCategories() {
        return categoryRepository.countActiveCategories();
    }

    // Helper: Tạo slug
    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }
}