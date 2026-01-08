package com.argaty.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Cart;

/**
 * Repository cho Cart Entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionId(String sessionId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    boolean existsByUserId(Long userId);

    boolean existsBySessionId(String sessionId);

    // Tìm các giỏ hàng guest đã cũ (để cleanup)
    @Query("SELECT c FROM Cart c WHERE c.user IS NULL AND c.updatedAt < :before")
    List<Cart> findOldGuestCarts(@Param("before") LocalDateTime before);
}