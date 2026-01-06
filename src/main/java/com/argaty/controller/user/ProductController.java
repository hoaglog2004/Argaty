package com.argaty.controller. user;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java. util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework. data.domain.Pageable;
import org.springframework.data. domain.Sort;
import org. springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web. bind.annotation.GetMapping;
import org.springframework.web.bind. annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.argaty.dto.response.PageResponse;
import com.argaty.dto.response.ProductDetailResponse;
import com.argaty.dto.response.ProductResponse;
import com.argaty.dto.response.ReviewStatsResponse;
import com.argaty.entity.Brand;
import com.argaty.entity.Category;
import com.argaty.entity.Product;
import com.argaty.entity.Review;
import com.argaty.service.BrandService;
import com.argaty.service.CategoryService;
import com.argaty.service.ProductService;
import com.argaty.service.ReviewService;
import com.argaty.service.UserService;
import com.argaty.service.WishlistService;
import com.argaty.util.DtoMapper;

import lombok.RequiredArgsConstructor;

/**
 * Controller cho sản phẩm
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final UserService userService;

    private static final int PRODUCTS_PER_PAGE = 12;

    /**
     * Danh sách sản phẩm với filter và search
     */
    @GetMapping
    public String products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            Principal principal,
            Model model) {

        // Tạo sort
        Sort sortOrder = createSort(sort);
        Pageable pageable = PageRequest.of(page, PRODUCTS_PER_PAGE, sortOrder);

        // Lấy danh sách sản phẩm
        Page<Product> productPage;

        if (q != null && !q.trim().isEmpty()) {
            // Search
            productPage = productService.search(q. trim(), pageable);
            model.addAttribute("searchKeyword", q);
        } else if (category != null && !category. isEmpty()) {
            // Filter by category
            Category cat = categoryService.findBySlug(category).orElse(null);
            if (cat != null) {
                productPage = productService.findByCategory(cat. getId(), pageable);
                model.addAttribute("currentCategory", DtoMapper.toCategoryResponse(cat));
            } else {
                productPage = productService.findActiveProducts(pageable);
            }
        } else if (brand != null && !brand. isEmpty()) {
            // Filter by brand
            Brand br = brandService.findBySlug(brand).orElse(null);
            if (br != null) {
                productPage = productService.findByBrand(br.getId(), pageable);
                model.addAttribute("currentBrand", DtoMapper.toBrandResponse(br));
            } else {
                productPage = productService. findActiveProducts(pageable);
            }
        } else if (minPrice != null || maxPrice != null) {
            // Filter by price
            BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
            BigDecimal max = maxPrice != null ?  maxPrice : BigDecimal.valueOf(Long.MAX_VALUE);
            productPage = productService.findByPriceRange(min, max, pageable);
        } else {
            productPage = productService.findActiveProducts(pageable);
        }

        // Convert to DTO
        PageResponse<ProductResponse> pageResponse = DtoMapper.toProductPageResponse(productPage);
        model.addAttribute("products", pageResponse);

        // Wishlist IDs nếu đã đăng nhập
        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(user -> {
                List<Long> wishlistIds = wishlistService.getWishlistProductIds(user.getId());
                model.addAttribute("wishlistIds", wishlistIds);
            });
        }

        // Categories và Brands cho sidebar filter
        model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));

        // Filter params
        model.addAttribute("currentSort", sort);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentPage", "products");

        return "user/products";
    }

    /**
     * Trang sản phẩm sale
     */
    @GetMapping("/sale")
    public String saleProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            Principal principal,
            Model model) {

        Pageable pageable = PageRequest. of(page, PRODUCTS_PER_PAGE);
        Page<Product> productPage = productService.findOnSale(pageable);

        model.addAttribute("products", DtoMapper.toProductPageResponse(productPage));
        model.addAttribute("pageTitle", "Sản phẩm đang giảm giá");
        model.addAttribute("currentPage", "sale");

        // Wishlist IDs
        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("wishlistIds", wishlistService.getWishlistProductIds(user.getId()));
            });
        }

        return "user/products";
    }

    /**
     * Chi tiết sản phẩm
     */
    @GetMapping("/{slug}")
    public String productDetail(
            @PathVariable String slug,
            Principal principal,
            Model model) {

        final Product product = productService. findBySlug(slug)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Product", "slug", slug));

        // Ensure details are loaded (images/variants/category/brand) to avoid LazyInitialization issues
        Product productWithDetails = productService.findBySlugWithDetails(slug)
            .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Product", "slug", slug));

        // Product detail
        ProductDetailResponse productDetail = DtoMapper.toProductDetailResponse(product);
        model.addAttribute("product", productDetail);

        // Reviews
        Page<Review> reviews = reviewService. findByProductId(product.getId(), PageRequest.of(0, 5));
        model.addAttribute("reviews", DtoMapper.toReviewPageResponse(reviews));

        // Review stats
        Double avgRating = reviewService.getAverageRating(product. getId());
        long reviewCount = reviewService.getReviewCount(product.getId());
        List<Object[]> ratingDist = reviewService.getRatingDistribution(product.getId());

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        for (Object[] row : ratingDist) {
            distribution.put((Integer) row[0], (Long) row[1]);
        }

        model.addAttribute("reviewStats", ReviewStatsResponse.create(avgRating, reviewCount, distribution));

        // Related products
        List<Product> relatedProducts = productService.findRelatedProducts(product.getId(), 4);
        model.addAttribute("relatedProducts", DtoMapper.toProductResponseList(relatedProducts));

        // Check wishlist và can review
        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("isInWishlist", wishlistService. isInWishlist(user.getId(), productWithDetails.getId()));
                model. addAttribute("canReview", reviewService.canUserReviewProduct(user.getId(), productWithDetails.getId()));
                model.addAttribute("hasReviewed", reviewService.hasUserReviewedProduct(user.getId(), productWithDetails.getId()));
            });
        }

        model.addAttribute("currentPage", "products");
        return "user/product-detail";
    }

    /**
     * Tìm kiếm sản phẩm (redirect)
     */
    @GetMapping("/search")
    public String search(@RequestParam String q) {
        return "redirect:/products? q=" + q;
    }

    /**
     * Tạo Sort từ parameter
     */
    private Sort createSort(String sort) {
        return switch (sort) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name-asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name-desc" -> Sort.by(Sort. Direction.DESC, "name");
            case "bestseller" -> Sort.by(Sort.Direction.DESC, "soldCount");
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            default -> Sort.by(Sort. Direction.DESC, "createdAt"); // newest
        };
    }
}