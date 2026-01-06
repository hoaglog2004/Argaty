package com.argaty.controller. api;

import com.argaty.dto.response.ApiResponse;
import com. argaty.dto.response.WishlistResponse;
import com. argaty.entity.User;
import com.argaty. entity.Wishlist;
import com.argaty.service.UserService;
import com.argaty.service.WishlistService;
import com.argaty.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST API Controller cho wishlist
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {

    private final WishlistService wishlistService;
    private final UserService userService;

    /**
     * Lấy danh sách wishlist
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getWishlist(Principal principal) {
        User user = getCurrentUser(principal);
        List<Wishlist> wishlists = wishlistService.findByUserIdWithProduct(user.getId());
        return ResponseEntity.ok(ApiResponse. success(DtoMapper.toWishlistResponseList(wishlists)));
    }

    /**
     * Lấy danh sách product IDs trong wishlist
     */
    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getWishlistIds(Principal principal) {
        User user = getCurrentUser(principal);
        List<Long> ids = wishlistService.getWishlistProductIds(user.getId());
        return ResponseEntity.ok(ApiResponse.success(ids));
    }

    /**
     * Thêm vào wishlist
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @PathVariable Long productId,
            Principal principal) {

        User user = getCurrentUser(principal);
        Wishlist wishlist = wishlistService.addToWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào yêu thích", 
                DtoMapper.toWishlistResponse(wishlist)));
    }

    /**
     * Xóa khỏi wishlist
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long productId,
            Principal principal) {

        User user = getCurrentUser(principal);
        wishlistService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khỏi yêu thích"));
    }

    /**
     * Toggle wishlist
     */
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggleWishlist(
            @PathVariable Long productId,
            Principal principal) {

        User user = getCurrentUser(principal);
        wishlistService.toggleWishlist(user.getId(), productId);
        boolean isInWishlist = wishlistService. isInWishlist(user. getId(), productId);
        
        String message = isInWishlist ?  "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
        return ResponseEntity.ok(ApiResponse.success(message, isInWishlist));
    }

    /**
     * Kiểm tra sản phẩm có trong wishlist không
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @PathVariable Long productId,
            Principal principal) {

        User user = getCurrentUser(principal);
        boolean isInWishlist = wishlistService.isInWishlist(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(isInWishlist));
    }

    /**
     * Đếm số lượng wishlist
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> countWishlist(Principal principal) {
        User user = getCurrentUser(principal);
        int count = wishlistService. countByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new com.argaty.exception.UnauthorizedException("Vui lòng đăng nhập");
        }
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));
    }
}