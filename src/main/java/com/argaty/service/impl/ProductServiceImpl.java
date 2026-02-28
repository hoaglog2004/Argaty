package com.argaty.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.argaty.dto.request.ProductRequest;
import com.argaty.dto.request.ProductVariantDTO;
import com.argaty.entity.Brand;
import com.argaty.entity.Category; // Cần import DTO này
import com.argaty.entity.Product;
import com.argaty.entity.ProductImage;
import com.argaty.entity.ProductVariant;
import com.argaty.entity.VariantImage;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.BrandRepository;
import com.argaty.repository.CategoryRepository;
import com.argaty.repository.ProductImageRepository;
import com.argaty.repository.ProductRepository;
import com.argaty.repository.ProductVariantRepository;
import com.argaty.repository.ReviewRepository;
import com.argaty.repository.VariantImageRepository;
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
    private final VariantImageRepository variantImageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;
    private final ProductDeletionTxService productDeletionTxService;

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
        return productRepository.findAllWithCategoryAndBrand(pageable);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        productDeletionTxService.hardDelete(id);
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
        List<Product> products = productRepository.findFeaturedProducts(PageRequest.of(0, limit));
        if (products.isEmpty()) {
            // Fallback: Lấy sản phẩm active mới nhất nếu chưa set featured
            return productRepository.findByIsActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, limit)).getContent();
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findNewProducts(int limit) {
        List<Product> products = productRepository.findNewProducts(PageRequest.of(0, limit));
        if (products.isEmpty()) {
            // Fallback: Lấy sản phẩm active mới nhất
            return productRepository.findByIsActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, limit)).getContent();
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findBestSellerProducts(int limit) {
        List<Product> products = productRepository.findBestSellerProducts(PageRequest.of(0, limit));
        if (products.isEmpty()) {
            // Fallback: Lấy sản phẩm bán chạy nhất hoặc active mới nhất
            return productRepository.findByIsActiveTrueOrderBySoldCountDesc(PageRequest.of(0, limit)).getContent();
        }
        return products;
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

    // ========== ADMIN METHODS (bao gồm cả inactive) ==========

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchAll(String keyword, Pageable pageable) {
        return productRepository.searchAllProducts(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAllByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findAllByCategoryAndSubcategories(categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAllByBrand(Long brandId, Pageable pageable) {
        return productRepository.findByBrandId(brandId, pageable);
    }

    // ========== CREATE & UPDATE ==========

    @Override
    public Product create(String name,String sku, String shortDescription, String description,
                          BigDecimal price, BigDecimal salePrice, Integer discountPercent,
                          Integer quantity, Long categoryId, Long brandId,
                          Boolean isFeatured, Boolean isNew, Boolean isBestSeller,
                          String specifications, String metaTitle, String metaDescription,
                          java.time.LocalDateTime saleStartDate, java.time.LocalDateTime saleEndDate,
                          String tier1Name, String tier2Name) {

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
            .sku(sku)
            .tier1Name(tier1Name)
            .tier2Name(tier2Name)
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
                .isBestSeller(isBestSeller != null && isBestSeller)
                .isActive(true)
                .specifications(specifications)
                .metaTitle(metaTitle)
                .metaDescription(metaDescription)
                .saleStartDate(saleStartDate)
                .saleEndDate(saleEndDate)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Created product: {}", name);

        return savedProduct;
    }

   @Override
    public Product update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // 1. Cập nhật thông tin cơ bản (Tên, Giá, Mô tả...)
        updateBasicInfo(product, request);

        // 2. XỬ LÝ ẢNH SẢN PHẨM (IMAGES)
        Set<Long> keptImageIds = (request.getExistingImageIds() != null) 
                ? new HashSet<>(request.getExistingImageIds()) 
                : new HashSet<>();
        
        // A. Xóa ảnh cũ không được giữ lại
        product.getImages().removeIf(img -> !keptImageIds.contains(img.getId()));

        // B. Cập nhật thứ tự ảnh cũ (Nếu cần chính xác thứ tự từ UI)
        if (request.getExistingImageIds() != null) {
            for (int i = 0; i < request.getExistingImageIds().size(); i++) {
                final Long imgId = request.getExistingImageIds().get(i); // Make imgId effectively final
                final int displayOrder = i; // Make displayOrder effectively final
                product.getImages().stream()
                        .filter(img -> img.getId().equals(imgId))
                        .findFirst()
                        .ifPresent(img -> img.setDisplayOrder(displayOrder));
            }
        }

        // C. Thêm ảnh mới
        int startOrder = product.getImages().size();
        if (request.getImageUrls() != null) {
            for (String url : request.getImageUrls()) {
                ProductImage newImg = ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .isMain(false)
                        .displayOrder(startOrder++)
                        .build();
                product.getImages().add(newImg);
            }
        }
        
        // Đảm bảo có ít nhất 1 ảnh là main
        if (!product.getImages().isEmpty() && product.getImages().stream().noneMatch(ProductImage::getIsMain)) {
            product.getImages().iterator().next().setIsMain(true);
        }

        // 3. XỬ LÝ BIẾN THỂ (VARIANTS)
        if (request.getVariants() != null) {
            Set<Long> incomingVariantIds = request.getVariants().stream()
                    .filter(Objects::nonNull)
                    .map(ProductVariantDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // A. Xóa biến thể không còn tồn tại
            product.getVariants().removeIf(v -> !incomingVariantIds.contains(v.getId()));

            // B. Cập nhật hoặc Thêm mới
            for (ProductVariantDTO vDto : request.getVariants()) {
                if (vDto == null ||
                        (vDto.getName() == null || vDto.getName().trim().isEmpty()) &&
                        (vDto.getColor() == null || vDto.getColor().trim().isEmpty()) &&
                        (vDto.getSize() == null || vDto.getSize().trim().isEmpty())) {
                    continue;
                }

                ProductVariant variantToSave;

                if (vDto.getId() != null) {
                    // --- CẬP NHẬT BIẾN THỂ CŨ ---
                    variantToSave = product.getVariants().stream()
                            .filter(v -> v.getId().equals(vDto.getId()))
                            .findFirst().orElse(null);

                    if (variantToSave != null) {
                        variantToSave.setName(vDto.getName());
                        variantToSave.setSku(vDto.getSku());
                        variantToSave.setColor(vDto.getColor());
                        variantToSave.setColorCode(vDto.getColorCode());
                        variantToSave.setSize(vDto.getSize());
                        variantToSave.setAdditionalPrice(vDto.getAdditionalPrice() != null ? vDto.getAdditionalPrice() : BigDecimal.ZERO);
                        variantToSave.setQuantity(vDto.getQuantity() != null ? vDto.getQuantity() : Integer.valueOf(0));
                    }
                } else {
                    // --- TẠO BIẾN THỂ MỚI ---
                    variantToSave = ProductVariant.builder()
                            .product(product)
                            .name(vDto.getName())
                            .sku(vDto.getSku())
                            .color(vDto.getColor())
                            .colorCode(vDto.getColorCode())
                            .size(vDto.getSize())
                            .additionalPrice(vDto.getAdditionalPrice() != null ? vDto.getAdditionalPrice() : BigDecimal.ZERO)
                            .quantity(vDto.getQuantity() != null ? vDto.getQuantity() : Integer.valueOf(0))
                            .isActive(true)
                            .displayOrder(product.getVariants().size())
                            .build();
                    product.getVariants().add(variantToSave);
                }

                // ========================================================
                // [QUAN TRỌNG] CẬP NHẬT ẢNH BIẾN THỂ (PHẦN BẠN ĐANG THIẾU)
                // ========================================================
                if (variantToSave != null && vDto.getImageUrls() != null && !vDto.getImageUrls().isEmpty()) {
                    // 1. Xóa ảnh biến thể cũ (Nếu muốn thay thế hoàn toàn)
                    variantToSave.getImages().clear();

                    // 2. Thêm ảnh mới từ DTO
                    for (String url : vDto.getImageUrls()) {
                        VariantImage vImg = VariantImage.builder()
                                .variant(variantToSave)
                                .imageUrl(url)
                                .isMain(true) // Mặc định ảnh biến thể là main
                                .build();
                        variantToSave.getImages().add(vImg);
                    }
                }
            }
        } else {
            product.getVariants().clear();
        }

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            int totalVariantStock = product.getVariants().stream()
                    .filter(ProductVariant::getIsActive)
                    .map(ProductVariant::getQuantity)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            product.setQuantity(totalVariantStock);
        }

        return productRepository.save(product);
    }
    private void updateBasicInfo(Product product, ProductRequest request) {
        // Cập nhật Slug nếu tên đổi
        if (!product.getName().equals(request.getName())) {
            String slug = SlugUtil.toSlug(request.getName());
            int count = 1;
            String originalSlug = slug;
            while (productRepository.existsBySlug(slug) && !slug.equals(product.getSlug())) {
                slug = originalSlug + "-" + count++;
            }
            product.setSlug(slug);
        }

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setTier1Name(request.getTier1Name());
        product.setTier2Name(request.getTier2Name());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setDiscountPercent(request.getDiscountPercent());
        if(request.getQuantity() != null) product.setQuantity(request.getQuantity());
        
        // Update Category
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        // Update Brand
        if (request.getBrandId() != null) {
            // Kiểm tra nếu brandId khác cũ thì mới query update
            if (product.getBrand() == null || !request.getBrandId().equals(product.getBrand().getId())) {
                Brand brand = brandRepository.findById(request.getBrandId())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
                product.setBrand(brand);
            }
        } else {
            product.setBrand(null);
        }

        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getIsNew() != null) product.setIsNew(request.getIsNew());
        if (request.getIsBestSeller() != null) product.setIsBestSeller(request.getIsBestSeller());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive()); // Map thêm cái này

        product.setSpecifications(request.getSpecifications());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setSaleStartDate(request.getSaleStartDate());
        product.setSaleEndDate(request.getSaleEndDate());
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
    public ProductVariant addVariant(Long productId, String name, String sku, String color, String colorCode,
                                     String size, BigDecimal additionalPrice, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .name(name)
                .sku(sku)
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
    public VariantImage addVariantImage(Long variantId, String imageUrl, boolean isMain) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

        if (isMain) {
            variantImageRepository.clearMainImage(variantId);
        }

        VariantImage image = VariantImage.builder()
                .variant(variant)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .displayOrder(variant.getImages() != null ? variant.getImages().size() : 0)
                .build();

        VariantImage saved = variantImageRepository.save(image);
        log.info("Added variant image {} to variant {}", saved.getId(), variantId);
        return saved;
    }

    @Override
    public ProductVariant updateVariant(Long variantId, String name, String sku, String color, String colorCode,
                                        String size, BigDecimal additionalPrice, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

        variant.setName(name);
        variant.setSku(sku);
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
    @Override
    @Transactional(readOnly = true)
    public Page<Product> filterProducts(String keyword, Long categoryId, Long brandId, 
                                        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        // Xử lý keyword rỗng thành null để query JPA bỏ qua
        String finalKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return productRepository.filterProducts(finalKeyword, categoryId, brandId, minPrice, maxPrice, pageable);
    }
     
}