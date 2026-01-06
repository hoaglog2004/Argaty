package com.argaty.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.argaty.entity.Cart;
import com.argaty.entity.CartItem;

/**
 * Service interface cho Cart
 */
public interface CartService {

    // ========== CART OPERATIONS ==========

    Cart getOrCreateCart(Long userId);

    Cart getOrCreateCartBySession(String sessionId);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionId(String sessionId);

    Cart findByUserIdWithItems(Long userId);

    Cart findBySessionIdWithItems(String sessionId);

    // ========== CART ITEM OPERATIONS ==========

    CartItem addItem(Long cartId, Long productId, Long variantId, int quantity);

    CartItem updateItemQuantity(Long cartItemId, int quantity);

    void removeItem(Long cartItemId);

    void toggleItemSelected(Long cartItemId);

    void selectAllItems(Long cartId, boolean selected);

    void clearCart(Long cartId);

    void clearSelectedItems(Long cartId);

    // ========== CART INFO ==========

    List<CartItem> getCartItems(Long cartId);

    int getCartItemCount(Long cartId);

    BigDecimal getCartTotal(Long cartId);

    BigDecimal getSelectedItemsTotal(Long cartId);

    // ========== MERGE CART ==========

    void mergeGuestCartToUser(String sessionId, Long userId);

    // ========== VALIDATION ==========

    boolean validateCartItems(Long cartId);
}