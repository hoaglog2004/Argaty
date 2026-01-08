package com.argaty.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.argaty.entity.Category;
import com.argaty.entity.Product;
import com.argaty.repository.CategoryRepository;
import com.argaty.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Khởi tạo dữ liệu và cấu trúc thư mục khi ứng dụng start
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppProperties appProperties;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String...args) throws Exception {
        log.info("🚀 Initializing Argaty application...");
        
        // Tạo các thư mục upload
        createUploadDirectories();
        
        // Tự động set flags nếu chưa có dữ liệu featured
        initializeProductFlags();
        initializeCategoryFlags();
        
        log.info("✅ Initialization completed!");
    }

    /**
     * Tạo các thư mục upload nếu chưa tồn tại
     */
    private void createUploadDirectories() {
        String[] directories = {
            appProperties.getUpload().getDir(),
            appProperties.getUpload().getProductImages(),
            appProperties.getUpload().getUserAvatars(),
            appProperties.getUpload().getBanners(),
            appProperties.getUpload().getReviews()
        };

        for (String dir : directories) {
            try {
                Path path = Paths.get(dir);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    log.info("📁 Created directory: {}", dir);
                }
            } catch (IOException e) {
                log.error("❌ Failed to create directory: {}", dir, e);
            }
        }
    }
    
    /**
     * Tự động set product flags nếu chưa có sản phẩm featured/new/bestseller
     */
    private void initializeProductFlags() {
        // Check và set Featured products
        List<Product> featuredProducts = productRepository.findFeaturedProducts(PageRequest.of(0, 1));
        if (featuredProducts.isEmpty()) {
            log.info("📦 No featured products found, auto-setting...");
            List<Product> productsToFeature = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, 8)).getContent();
            for (Product p : productsToFeature) {
                p.setIsFeatured(true);
                productRepository.save(p);
            }
            log.info("✅ Set {} products as featured", productsToFeature.size());
        }
        
        // Check và set New products
        List<Product> newProducts = productRepository.findNewProducts(PageRequest.of(0, 1));
        if (newProducts.isEmpty()) {
            log.info("📦 No new products found, auto-setting...");
            List<Product> productsToNew = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, 8)).getContent();
            for (Product p : productsToNew) {
                p.setIsNew(true);
                productRepository.save(p);
            }
            log.info("✅ Set {} products as new", productsToNew.size());
        }
        
        // Check và set Best Seller products
        List<Product> bestSellers = productRepository.findBestSellerProducts(PageRequest.of(0, 1));
        if (bestSellers.isEmpty()) {
            log.info("📦 No bestseller products found, auto-setting...");
            List<Product> productsToBestSeller = productRepository.findByIsActiveTrueOrderBySoldCountDesc(PageRequest.of(0, 8)).getContent();
            for (Product p : productsToBestSeller) {
                p.setIsBestSeller(true);
                productRepository.save(p);
            }
            log.info("✅ Set {} products as bestseller", productsToBestSeller.size());
        }
    }
    
    /**
     * Tự động set category flags nếu chưa có category featured
     */
    private void initializeCategoryFlags() {
        List<Category> featuredCategories = categoryRepository.findFeaturedCategories();
        if (featuredCategories.isEmpty()) {
            log.info("📂 No featured categories found, auto-setting...");
            List<Category> categoriesToFeature = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
            int count = 0;
            for (Category c : categoriesToFeature) {
                if (count >= 6) break;
                c.setIsFeatured(true);
                categoryRepository.save(c);
                count++;
            }
            log.info("✅ Set {} categories as featured", count);
        }
    }
}