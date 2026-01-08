package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.ProductVariant;

/**
 * Repository cho ProductVariant Entity
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderByDisplayOrderAsc(Long productId);

    List<ProductVariant> findByProductIdAndIsActiveTrueOrderByDisplayOrderAsc(Long productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = : productId AND pv.color = :color")
    List<ProductVariant> findByProductIdAndColor(@Param("productId") Long productId, 
                                                  @Param("color") String color);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.quantity = pv.quantity - :quantity " +
           "WHERE pv.id = :variantId AND pv.quantity >= :quantity")
    int decreaseQuantity(@Param("variantId") Long variantId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.quantity = pv.quantity + :quantity WHERE pv.id = :variantId")
    void increaseQuantity(@Param("variantId") Long variantId, @Param("quantity") int quantity);

    @Query("SELECT SUM(pv.quantity) FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isActive = true")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);

    int countByProductId(Long productId);
}