package com.argaty.service.impl;

import com.argaty.entity.Product;
import com.argaty.entity.User;
import com.argaty.entity.Wishlist;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.ProductRepository;
import com.argaty.repository.UserRepository;
import com.argaty.repository.WishlistRepository;
import com.argaty.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation của WishlistService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Wishlist addToWishlist(Long userId, Long productId) {
        // Kiểm tra đã có trong wishlist chưa
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return wishlistRepository.findByUserIdAndProductId(userId, productId).get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        log.info("Added product {} to wishlist for user {}", productId, userId);
        return wishlistRepository.save(wishlist);
    }

    @Override
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Removed product {} from wishlist for user {}", productId, userId);
    }

    @Override
    public void toggleWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            removeFromWishlist(userId, productId);
        } else {
            addToWishlist(userId, productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Wishlist> findByUserId(Long userId, Pageable pageable) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> findByUserIdWithProduct(Long userId) {
        return wishlistRepository.findByUserIdWithProduct(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWishlistProductIds(Long userId) {
        return wishlistRepository.findProductIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByUserId(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }

    @Override
    public void clearWishlist(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdWithProduct(userId);
        wishlistRepository.deleteAll(wishlists);
        log.info("Cleared wishlist for user {}", userId);
    }
}