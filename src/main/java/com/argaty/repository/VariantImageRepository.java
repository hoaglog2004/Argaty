package com.argaty.repository;

import java.util. List;
import java.util.Optional;

import org.springframework.data.jpa. repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data. repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.VariantImage;

/**
 * Repository cho VariantImage Entity
 */
@Repository
public interface VariantImageRepository extends JpaRepository<VariantImage, Long> {

    List<VariantImage> findByVariantIdOrderByDisplayOrderAsc(Long variantId);

    Optional<VariantImage> findByVariantIdAndIsMainTrue(Long variantId);

    @Modifying
    @Query("UPDATE VariantImage vi SET vi.isMain = false WHERE vi.variant.id = :variantId")
    void clearMainImage(@Param("variantId") Long variantId);

    @Modifying
    @Query("DELETE FROM VariantImage vi WHERE vi.variant.id = :variantId")
    void deleteByVariantId(@Param("variantId") Long variantId);
}