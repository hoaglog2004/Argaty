package com.argaty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Cart - Giỏ hàng
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Column(name = "session_id", length = 100)
    private String sessionId;

    // ========== RELATIONSHIPS ==========

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Tính tổng tiền giỏ hàng
     */
    public BigDecimal getTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .filter(CartItem::getIsSelected)
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính tổng tiền tất cả sản phẩm (kể cả không chọn)
     */
    public BigDecimal getAllItemsTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Đếm số lượng sản phẩm đã chọn
     */
    public int getSelectedItemCount() {
        if (items == null) {
            return 0;
        }

        return items.stream()
                .filter(CartItem::getIsSelected)
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Đếm tổng số sản phẩm trong giỏ
     */
    public int getTotalItemCount() {
        if (items == null) {
            return 0;
        }

        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Kiểm tra giỏ hàng trống
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * Thêm item vào giỏ
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    /**
     * Xóa item khỏi giỏ
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /**
     * Xóa tất cả items đã chọn
     */
    public void clearSelectedItems() {
        items.removeIf(CartItem::getIsSelected);
    }

    /**
     * Xóa tất cả items
     */
    public void clearAll() {
        items.clear();
    }
}