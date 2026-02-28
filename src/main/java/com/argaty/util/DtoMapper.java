package com.argaty.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.argaty.dto.response.BannerResponse;
import com.argaty.dto.response.BrandResponse;
import com.argaty.dto.response.CartItemResponse;
import com.argaty.dto.response.CartResponse;
import com.argaty.dto.response.CategoryResponse;
import com.argaty.dto.response.NotificationResponse;
import com.argaty.dto.response.OrderDetailResponse;
import com.argaty.dto.response.OrderResponse;
import com.argaty.dto.response.PageResponse;
import com.argaty.dto.response.ProductDetailResponse;
import com.argaty.dto.response.ProductResponse;
import com.argaty.dto.response.ReviewResponse;
import com.argaty.dto.response.UserAddressResponse;
import com.argaty.dto.response.UserResponse;
import com.argaty.dto.response.VoucherResponse;
import com.argaty.dto.response.WishlistResponse;
import com.argaty.entity.Banner;
import com.argaty.entity.Brand;
import com.argaty.entity.Cart;
import com.argaty.entity.CartItem;
import com.argaty.entity.Category;
import com.argaty.entity.Notification;
import com.argaty.entity.Order;
import com.argaty.entity.Product;
import com.argaty.entity.ProductVariant;
import com.argaty.entity.Review;
import com.argaty.entity.User;
import com.argaty.entity.UserAddress;
import com.argaty.entity.VariantImage;
import com.argaty.entity.Voucher;
import com.argaty.entity.Wishlist;

/**
 * Utility class để convert Entity sang DTO
 */
public class DtoMapper {

    private DtoMapper() {
        // Private constructor để prevent instantiation
    }

    // ========== USER ==========

    public static UserResponse toUserResponse(User user) {
        return UserResponse.fromEntity(user);
    }

    public static List<UserResponse> toUserResponseList(List<User> users) {
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public static PageResponse<UserResponse> toUserPageResponse(Page<User> page) {
        List<UserResponse> content = page.getContent().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    // ========== PRODUCT ==========

    public static ProductResponse toProductResponse(Product product) {
    return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .sku(product.getSku())
            .price(product.getPrice())
            .salePrice(product.getSalePrice())
            .quantity(product.getQuantity())
            .isActive(product.getIsActive())
            .isFeatured(product.getIsFeatured())
            .isNew(product.getIsNew())
            .isBestSeller(product.getIsBestSeller())
            
            // --- SỬA ĐOẠN NÀY ĐỂ TRÁNH LỖI NULL POINTER ---
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Chưa phân loại")
            .brandName(product.getBrand() != null ? product.getBrand().getName() : "N/A")
            
            // Xử lý ảnh an toàn
            .mainImage((product.getImages() != null && !product.getImages().isEmpty()) 
                            ? product.getImages().iterator().next().getImageUrl() 
                            : "/images/no-image.png")
                .build();
}

    public static List<ProductResponse> toProductResponseList(List<Product> products) {
        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public static PageResponse<ProductResponse> toProductPageResponse(Page<Product> page) {
        List<ProductResponse> content = page.getContent().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    public static ProductDetailResponse toProductDetailResponse(Product product) {
        // 1. Map thông tin cơ bản
        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .tier1Name(product.getTier1Name())
                .tier2Name(product.getTier2Name())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .discountPercent(product.getCalculatedDiscountPercent())
                .quantity(product.getQuantity())
                .soldCount(product.getSoldCount())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isNew(product.getIsNew())
                .isFeatured(product.getIsFeatured())
                .isBestSeller(product.getIsBestSeller())
                .isOnSale(product.isOnSale())
                .isInStock(product.isInStock())
                .isLowStock(product.isLowStock())
                .specifications(product.getSpecifications())
                .saleStartDate(product.getSaleStartDate())
                .saleEndDate(product.getSaleEndDate())
                .createdAt(product.getCreatedAt())
                .build();

        // 2. Map Category & Brand
        if (product.getCategory() != null) {
            response.setCategory(CategoryResponse.fromEntity(product.getCategory()));
        }
        if (product.getBrand() != null) {
            response.setBrand(BrandResponse.fromEntity(product.getBrand()));
        }

        // 3. Map Images
        if (product.getImages() != null) {
            response.setImages(product.getImages().stream()
                    .map(img -> ProductDetailResponse.ImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isMain(img.getIsMain())
                            .displayOrder(img.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        // 4. Map Variants & Gom Nhóm (Unique Colors/Sizes)
        List<String> uniqueColors = new ArrayList<>();
        List<String> uniqueSizes = new ArrayList<>();

        if (product.getVariants() != null) {
            // A. Map danh sách biến thể
            List<ProductDetailResponse.VariantResponse> variantResponses = product.getVariants().stream()
                    .filter(ProductVariant::getIsActive)
                    .map(v -> {
                        // Lấy ảnh đại diện của biến thể (nếu có)
                        String variantImg = null;
                        // Kiểm tra xem biến thể có danh sách ảnh không (List<VariantImage>)
                        if (v.getImages() != null && !v.getImages().isEmpty()) {
                            variantImg = v.getImages().get(0).getImageUrl();
                        }

                        return ProductDetailResponse.VariantResponse.builder()
                                .id(v.getId())
                                .name(v.getName())
                                .sku(v.getSku())
                                .color(v.getColor())
                                .colorCode(v.getColorCode())
                                .size(v.getSize())
                                .additionalPrice(v.getAdditionalPrice())
                                .finalPrice(v.getFinalPrice())
                                .quantity(v.getQuantity())
                                .isActive(v.getIsActive())
                                .isInStock(v.isInStock())
                            .images(v.getImages() != null
                                ? v.getImages().stream().map(VariantImage::getImageUrl).collect(Collectors.toList())
                                : null)
                                .imageUrl(variantImg) // Gán ảnh vào DTO để JS xử lý đổi ảnh
                                .build();
                    })
                    .collect(Collectors.toList());
            
            response.setVariants(variantResponses);

            // B. Gom nhóm màu sắc duy nhất
            uniqueColors = product.getVariants().stream()
                    .map(ProductVariant::getColor)
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            // C. Gom nhóm size/phiên bản duy nhất
            uniqueSizes = product.getVariants().stream()
                    .map(ProductVariant::getSize)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }

        response.setUniqueColors(uniqueColors);
        response.setUniqueSizes(uniqueSizes);

        return response;
    }

    // ========== CATEGORY ==========

    public static CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.fromEntity(category);
    }

    public static List<CategoryResponse> toCategoryResponseList(List<Category> categories) {
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public static List<CategoryResponse> toCategoryWithChildrenResponseList(List<Category> categories) {
        return categories.stream()
                .map(CategoryResponse::fromEntityWithChildren)
                .collect(Collectors.toList());
    }

    public static PageResponse<CategoryResponse> toCategoryPageResponse(Page<Category> page) {
        List<CategoryResponse> content = page.getContent().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    // ========== BRAND ==========

    public static BrandResponse toBrandResponse(Brand brand) {
        return BrandResponse.fromEntity(brand);
    }

    public static List<BrandResponse> toBrandResponseList(List<Brand> brands) {
        return brands.stream()
                .map(BrandResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== CART ==========

    public static CartResponse toCartResponse(Cart cart) {
        return CartResponse.fromEntity(cart);
    }

    public static CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.fromEntity(item);
    }

    // ========== ORDER ==========

    public static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.fromEntity(order);
    }

    public static List<OrderResponse> toOrderResponseList(List<Order> orders) {
        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public static PageResponse<OrderResponse> toOrderPageResponse(Page<Order> page) {
        List<OrderResponse> content = page.getContent().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    public static OrderDetailResponse toOrderDetailResponse(Order order) {
        return OrderDetailResponse.fromEntity(order);
    }

    // ========== REVIEW ==========

    public static ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.fromEntity(review);
    }

    public static List<ReviewResponse> toReviewResponseList(List<Review> reviews) {
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public static PageResponse<ReviewResponse> toReviewPageResponse(Page<Review> page) {
        List<ReviewResponse> content = page.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    // ========== VOUCHER ==========

    public static VoucherResponse toVoucherResponse(Voucher voucher) {
        return VoucherResponse.fromEntity(voucher);
    }

    public static List<VoucherResponse> toVoucherResponseList(List<Voucher> vouchers) {
        return vouchers.stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== NOTIFICATION ==========

    public static NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.fromEntity(notification);
    }

    public static List<NotificationResponse> toNotificationResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== BANNER ==========

    public static BannerResponse toBannerResponse(Banner banner) {
        return BannerResponse.fromEntity(banner);
    }

    public static List<BannerResponse> toBannerResponseList(List<Banner> banners) {
        return banners.stream()
                .map(BannerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== USER ADDRESS ==========

    public static UserAddressResponse toUserAddressResponse(UserAddress address) {
        return UserAddressResponse.fromEntity(address);
    }

    public static List<UserAddressResponse> toUserAddressResponseList(List<UserAddress> addresses) {
        return addresses.stream()
                .map(UserAddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== WISHLIST ==========

    public static WishlistResponse toWishlistResponse(Wishlist wishlist) {
        return WishlistResponse.fromEntity(wishlist);
    }

    public static List<WishlistResponse> toWishlistResponseList(List<Wishlist> wishlists) {
        return wishlists.stream()
                .map(WishlistResponse::fromEntity)
                .collect(Collectors.toList());
    }
}