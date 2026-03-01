package com.argaty.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.PasswordResetToken;

/**
 * Repository cho PasswordResetToken Entity
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token " +
           "AND prt.used = false AND prt.expiresAt > CURRENT_TIMESTAMP")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId " +
           "AND prt.used = false AND prt.expiresAt > CURRENT_TIMESTAMP " +
           "ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findLatestValidTokenByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.token = :token")
    void markAsUsed(@Param("token") String token);

    @Modifying
       @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByTokenAndUsedFalse(String token);
}