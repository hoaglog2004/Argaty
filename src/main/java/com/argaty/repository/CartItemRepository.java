package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.CartItem;

/**
 * Repository cho CartItem Entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCartIdAndIsSelectedTrue(Long cartId);

    @Query("SELECT ci FROM CartItem ci " +
           "LEFT JOIN FETCH ci.product " +
           "LEFT JOIN FETCH ci.variant " +
           "WHERE ci.cart.id = :cartId " +
           "ORDER BY ci.addedAt DESC")
    List<CartItem> findByCartIdWithProductAndVariant(@Param("cartId") Long cartId);

    Optional<CartItem> findByCartIdAndProductIdAndVariantId(Long cartId, Long productId, Long variantId);

    Optional<CartItem> findByCartIdAndProductIdAndVariantIsNull(Long cartId, Long productId);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = : quantity WHERE ci.id = :itemId")
    void updateQuantity(@Param("itemId") Long itemId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isSelected = : selected WHERE ci.id = :itemId")
    void updateSelected(@Param("itemId") Long itemId, @Param("selected") boolean selected);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isSelected = :selected WHERE ci.cart.id = :cartId")
    void updateAllSelected(@Param("cartId") Long cartId, @Param("selected") boolean selected);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.isSelected = true")
    void deleteSelectedItems(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    int countByCartId(Long cartId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalQuantity(@Param("cartId") Long cartId);
}