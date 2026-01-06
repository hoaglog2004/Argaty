package com.argaty.service;

import com. argaty.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain. Pageable;

import java.util.List;

/**
 * Service interface cho Wishlist
 */
public interface WishlistService {

    Wishlist addToWishlist(Long userId, Long productId);

    void removeFromWishlist(Long userId, Long productId);

    void toggleWishlist(Long userId, Long productId);

    boolean isInWishlist(Long userId, Long productId);

    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    List<Wishlist> findByUserIdWithProduct(Long userId);

    List<Long> getWishlistProductIds(Long userId);

    int countByUserId(Long userId);

    void clearWishlist(Long userId);
}