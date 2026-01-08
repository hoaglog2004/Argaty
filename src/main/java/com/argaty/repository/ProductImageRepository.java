package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.ProductImage;

/**
 * Repository cho ProductImage Entity
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    Optional<ProductImage> findByProductIdAndIsMainTrue(Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = false WHERE pi.product.id = :productId")
    void clearMainImage(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = true WHERE pi.id = :imageId")
    void setMainImage(@Param("imageId") Long imageId);

    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    int countByProductId(Long productId);
}