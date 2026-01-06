package com.argaty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework. data.jpa.repository. JpaRepository;
import org.springframework.data.jpa.repository. Modifying;
import org.springframework. data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Wishlist;

/**
 * Repository cho Wishlist Entity
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.product WHERE w.user.id = : userId ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdWithProduct(@Param("userId") Long userId);

    Page<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    int countByUserId(Long userId);

    int countByProductId(Long productId);

    @Query("SELECT w.product.id FROM Wishlist w WHERE w.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);
}