package com. argaty.util;

import com.argaty.dto.response.*;
import com.argaty.entity.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream. Collectors;

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
                .map(UserResponse:: fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    // ========== PRODUCT ==========

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.fromEntity(product);
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
        return ProductDetailResponse.fromEntity(product);
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
        return categories. stream()
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
        return BrandResponse. fromEntity(brand);
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
        return orders. stream()
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
        return PageResponse. of(page, content);
    }

    // ========== VOUCHER ==========

    public static VoucherResponse toVoucherResponse(Voucher voucher) {
        return VoucherResponse. fromEntity(voucher);
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
                .map(UserAddressResponse:: fromEntity)
                .collect(Collectors.toList());
    }

    // ========== WISHLIST ==========

    public static WishlistResponse toWishlistResponse(Wishlist wishlist) {
        return WishlistResponse. fromEntity(wishlist);
    }

    public static List<WishlistResponse> toWishlistResponseList(List<Wishlist> wishlists) {
        return wishlists.stream()
                .map(WishlistResponse::fromEntity)
                .collect(Collectors.toList());
    }
}