package com.argaty.service.impl;

import com.argaty.entity.*;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.exception.BadRequestException;
import com.argaty.repository.*;
import com.argaty.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của CartService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    // ========== CART OPERATIONS ==========

    @Override
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                    Cart cart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(cart);
                });
    }

    @Override
    public Cart getOrCreateCartBySession(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .sessionId(sessionId)
                            .build();

                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findBySessionId(String sessionId) {
        return cartRepository.findBySessionId(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart findByUserIdWithItems(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Cart findBySessionIdWithItems(String sessionId) {
        return cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));
    }

    // ========== CART ITEM OPERATIONS ==========

    @Override
    public CartItem addItem(Long cartId, Long productId, Long variantId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Kiểm tra sản phẩm còn active
        if (!product.getIsActive()) {
            throw new BadRequestException("Sản phẩm không còn khả dụng");
        }

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

            if (!variant.getIsActive()) {
                throw new BadRequestException("Phân loại sản phẩm không còn khả dụng");
            }
        }

        // Kiểm tra tồn kho
        int availableQty = variant != null ? variant.getQuantity() : product.getQuantity();
        if (quantity > availableQty) {
            throw new BadRequestException("Số lượng yêu cầu vượt quá tồn kho");
        }

        // Kiểm tra item đã có trong giỏ chưa
        Optional<CartItem> existingItem;
        if (variantId != null) {
            existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(cartId, productId, variantId);
        } else {
            existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantIsNull(cartId, productId);
        }

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Cập nhật số lượng
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (newQuantity > availableQty) {
                throw new BadRequestException("Số lượng trong giỏ vượt quá tồn kho");
            }

            cartItem.setQuantity(newQuantity);
            cartItem = cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity: {} -> {}", cartItem.getId(), newQuantity);
        } else {
            // Thêm mới
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantity(quantity)
                    .isSelected(true)
                    .build();

            cartItem = cartItemRepository.save(cartItem);
            log.info("Added item to cart: product={}, variant={}, qty={}", productId, variantId, quantity);
        }

        return cartItem;
    }

    @Override
    public CartItem updateItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (quantity <= 0) {
            throw new BadRequestException("Số lượng phải lớn hơn 0");
        }

        // Kiểm tra tồn kho
        int availableQty = cartItem.getAvailableQuantity();
        if (quantity > availableQty) {
            throw new BadRequestException("Số lượng yêu cầu vượt quá tồn kho (" + availableQty + ")");
        }

        cartItem.setQuantity(quantity);
        log.info("Updated cart item quantity: {} -> {}", cartItemId, quantity);

        return cartItemRepository.save(cartItem);
    }

    @Override
    public void removeItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new ResourceNotFoundException("CartItem", "id", cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
        log.info("Removed cart item: {}", cartItemId);
    }

    @Override
    public void toggleItemSelected(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        cartItem.setIsSelected(!cartItem.getIsSelected());
        cartItemRepository.save(cartItem);
    }

    @Override
    public void selectAllItems(Long cartId, boolean selected) {
        cartItemRepository.updateAllSelected(cartId, selected);
    }

    @Override
    public void clearCart(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
        log.info("Cleared cart: {}", cartId);
    }

    @Override
    public void clearSelectedItems(Long cartId) {
        cartItemRepository.deleteSelectedItems(cartId);
        log.info("Cleared selected items from cart: {}", cartId);
    }

    // ========== CART INFO ==========

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCartIdWithProductAndVariant(cartId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long cartId) {
        Integer count = cartItemRepository.getTotalQuantity(cartId);
        return count != null ? count : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSelectedItemsTotal(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartIdAndIsSelectedTrue(cartId);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== MERGE CART ==========

        // ========== MERGE CART (tiếp tục) ==========

    @Override
    public void mergeGuestCartToUser(String sessionId, Long userId) {
        Optional<Cart> guestCartOpt = cartRepository.findBySessionIdWithItems(sessionId);
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            return;
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = getOrCreateCart(userId);

        // Merge từng item từ guest cart vào user cart
        for (CartItem guestItem : guestCart.getItems()) {
            try {
                addItem(userCart.getId(),
                        guestItem.getProduct().getId(),
                        guestItem.getVariant() != null ? guestItem.getVariant().getId() : null,
                        guestItem.getQuantity());
            } catch (BadRequestException e) {
                // Bỏ qua nếu không thêm được (hết hàng, etc.)
                log.warn("Could not merge cart item: {}", e.getMessage());
            }
        }

        // Xóa guest cart
        cartRepository.delete(guestCart);
        log.info("Merged guest cart {} to user cart {}", sessionId, userId);
    }

    // ========== VALIDATION ==========

    @Override
    @Transactional(readOnly = true)
    public boolean validateCartItems(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartIdWithProductAndVariant(cartId);

        for (CartItem item : items) {
            // Kiểm tra sản phẩm còn active
            if (!item.getProduct().getIsActive()) {
                return false;
            }

            // Kiểm tra variant còn active
            if (item.getVariant() != null && !item.getVariant().getIsActive()) {
                return false;
            }

            // Kiểm tra tồn kho
            if (!item.isInStock()) {
                return false;
            }
        }

        return true;
    }
}