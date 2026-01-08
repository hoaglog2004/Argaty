package com.argaty.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.argaty.entity.Brand;
import com.argaty.entity.Category;
import com.argaty.entity.Product;
import com.argaty.entity.ProductImage;
import com.argaty.entity.ProductVariant;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.BrandRepository;
import com.argaty.repository.CategoryRepository;
import com.argaty.repository.ProductImageRepository;
import com.argaty.repository.ProductRepository;
import com.argaty.repository.ProductVariantRepository;
import com.argaty.repository.ReviewRepository;
import com.argaty.service.ProductService;
import com.argaty.util.SlugUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation của ProductService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;

    // ========== CRUD ==========

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlugAndIsActiveTrue(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySlugWithDetails(String slug) {
        return productRepository.findBySlugWithAllDetails(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByIdWithDetails(Long id) {
        return productRepository.findByIdWithAllDetails(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        log.info("Deleted product: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return productRepository.existsBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    // ========== FIND PRODUCTS ==========

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findActiveProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryAndSubcategories(categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByBrand(Long brandId, Pageable pageable) {
        return productRepository.findByBrandIdAndIsActiveTrue(brandId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findOnSale(Pageable pageable) {
        return productRepository.findOnSaleProducts(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts(int limit) {
        return productRepository.findFeaturedProducts(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findNewProducts(int limit) {
        return productRepository.findNewProducts(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findBestSellerProducts(int limit) {
        return productRepository.findBestSellerProducts(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return productRepository.findRelatedProducts(
                product.getCategory().getId(),
                productId,
                PageRequest.of(0, limit)
        );
    }

    // ========== SEARCH & FILTER ==========

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchByCategory(String keyword, Long categoryId, Pageable pageable) {
        return productRepository.searchProductsByCategory(keyword, categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }

    // ========== CREATE & UPDATE ==========

    @Override
    public Product create(String name, String shortDescription, String description,
                          BigDecimal price, BigDecimal salePrice, Integer discountPercent,
                          Integer quantity, Long categoryId, Long brandId,
                          Boolean isFeatured, Boolean isNew) {

        // Tạo slug
        String slug = SlugUtil.toSlug(name);
        int count = 1;
        String originalSlug = slug;
        while (productRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + count++;
        }

        // Lấy category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Lấy brand nếu có
        Brand brand = null;
        if (brandId != null) {
            brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
        }

        Product product = Product.builder()
                .name(name)
                .slug(slug)
                .shortDescription(shortDescription)
                .description(description)
                .price(price)
                .salePrice(salePrice)
                .discountPercent(discountPercent)
                .quantity(quantity != null ? quantity : 0)
                .category(category)
                .brand(brand)
                .isFeatured(isFeatured != null && isFeatured)
                .isNew(isNew != null && isNew)
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Created product: {}", name);

        return savedProduct;
    }

    @Override
    public Product update(Long id, String name, String shortDescription, String description,
                          BigDecimal price, BigDecimal salePrice, Integer discountPercent,
                          Integer quantity, Long categoryId, Long brandId,
                          Boolean isFeatured, Boolean isNew) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Cập nhật slug nếu tên thay đổi
        if (!product.getName().equals(name)) {
            String slug = SlugUtil.toSlug(name);
            int count = 1;
            String originalSlug = slug;
            while (productRepository.existsBySlug(slug) && !slug.equals(product.getSlug())) {
                slug = originalSlug + "-" + count++;
            }
            product.setSlug(slug);
        }

        product.setName(name);
        product.setShortDescription(shortDescription);
        product.setDescription(description);
        product.setPrice(price);
        product.setSalePrice(salePrice);
        product.setDiscountPercent(discountPercent);
        if (quantity != null) {
            product.setQuantity(quantity);
        }

        // Cập nhật category
        if (categoryId != null && !categoryId.equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
            product.setCategory(category);
        }

        // Cập nhật brand
        if (brandId != null) {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        if (isFeatured != null) {
            product.setIsFeatured(isFeatured);
        }
        if (isNew != null) {
            product.setIsNew(isNew);
        }

        log.info("Updated product: {}", id);
        return productRepository.save(product);
    }

    @Override
    public void toggleActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
        log.info("Toggled product active status: {} -> {}", id, product.getIsActive());
    }

    @Override
    public void toggleFeatured(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setIsFeatured(!product.getIsFeatured());
        productRepository.save(product);
        log.info("Toggled product featured status: {} -> {}", id, product.getIsFeatured());
    }

    @Override
    public void toggleNew(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setIsNew(!product.getIsNew());
        productRepository.save(product);
        log.info("Toggled product isNew status: {} -> {}", id, product.getIsNew());
    }

    // ========== IMAGES ==========

    @Override
    public ProductImage addImage(Long productId, String imageUrl, boolean isMain) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Nếu là ảnh chính, clear các ảnh chính khác
        if (isMain) {
            productImageRepository.clearMainImage(productId);
        }

        // Nếu là ảnh đầu tiên, set là ảnh chính
        if (product.getImages().isEmpty()) {
            isMain = true;
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .displayOrder(product.getImages().size())
                .build();

        return productImageRepository.save(image);
    }

    @Override
    public void removeImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        Long productId = image.getProduct().getId();
        boolean wasMain = image.getIsMain();

        productImageRepository.delete(image);

        // Nếu xóa ảnh chính, set ảnh khác làm chính
        if (wasMain) {
            List<ProductImage> remainingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
            if (!remainingImages.isEmpty()) {
                productImageRepository.setMainImage(remainingImages.get(0).getId());
            }
        }

        log.info("Removed product image: {}", imageId);
    }

    @Override
    public void setMainImage(Long productId, Long imageId) {
        productImageRepository.clearMainImage(productId);
        productImageRepository.setMainImage(imageId);
        log.info("Set main image for product {}: {}", productId, imageId);
    }

    // ========== VARIANTS ==========

    @Override
    public ProductVariant addVariant(Long productId, String name, String color, String colorCode,
                                     String size, BigDecimal additionalPrice, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .name(name)
                .color(color)
                .colorCode(colorCode)
                .size(size)
                .additionalPrice(additionalPrice != null ? additionalPrice : BigDecimal.ZERO)
                .quantity(quantity != null ? quantity :  0)
                .isActive(true)
                .displayOrder(product.getVariants().size())
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);
        log.info("Added variant to product {}: {}", productId, name);

        return savedVariant;
    }

    @Override
    public ProductVariant updateVariant(Long variantId, String name, String color, String colorCode,
                                        String size, BigDecimal additionalPrice, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

        variant.setName(name);
        variant.setColor(color);
        variant.setColorCode(colorCode);
        variant.setSize(size);
        if (additionalPrice != null) {
            variant.setAdditionalPrice(additionalPrice);
        }
        if (quantity != null) {
            variant.setQuantity(quantity);
        }

        log.info("Updated variant: {}", variantId);
        return productVariantRepository.save(variant);
    }

    @Override
    public void removeVariant(Long variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("ProductVariant", "id", variantId);
        }
        productVariantRepository.deleteById(variantId);
        log.info("Removed variant: {}", variantId);
    }

    // ========== STOCK ==========

    @Override
    public void decreaseStock(Long productId, Long variantId, int quantity) {
        if (variantId != null) {
            int updated = productVariantRepository.decreaseQuantity(variantId, quantity);
            if (updated == 0) {
                throw new BadRequestException("Không đủ số lượng tồn kho");
            }
        } else {
            int updated = productRepository.decreaseQuantity(productId, quantity);
            if (updated == 0) {
                throw new BadRequestException("Không đủ số lượng tồn kho");
            }
        }
        log.info("Decreased stock for product {} (variant {}): -{}", productId, variantId, quantity);
    }

    @Override
    public void increaseStock(Long productId, Long variantId, int quantity) {
        if (variantId != null) {
            productVariantRepository.increaseQuantity(variantId, quantity);
        } else {
            productRepository.increaseQuantity(productId, quantity);
        }
        log.info("Increased stock for product {} (variant {}): +{}", productId, variantId, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    // ========== RATING ==========

    @Override
    public void updateRating(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        long reviewCount = reviewRepository.countByProductId(productId);

        BigDecimal rating = avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        productRepository.updateRating(productId, rating, (int) reviewCount);
        log.info("Updated rating for product {}: {} ({} reviews)", productId, rating, reviewCount);
    }

    // ========== STATISTICS ==========

    @Override
    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countActiveProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public long countOutOfStockProducts() {
        return productRepository.countOutOfStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalStock() {
        return productRepository.getTotalStock();
    }
}