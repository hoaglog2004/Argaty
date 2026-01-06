package com.argaty.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data. jpa.repository. JpaRepository;
import org.springframework.data.jpa.repository. Modifying;
import org.springframework. data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.User;
import com.argaty.enums.Role;

/**
 * Repository cho User Entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== FIND BY FIELD ==========

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailAndIsEnabledTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ========== FIND BY ROLE ==========

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    long countByRole(Role role);

    // ========== FIND BY STATUS ==========

    List<User> findByIsEnabledTrue();

    List<User> findByIsBannedTrue();

    Page<User> findByIsBannedTrue(Pageable pageable);

    // ========== SEARCH ==========

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%')")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u. email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsersByRole(@Param("keyword") String keyword, 
                                  @Param("role") Role role, 
                                  Pageable pageable);

    // ========== STATISTICS ==========

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewUsersFromDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countNewUsersBetween(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u. role, COUNT(u) FROM User u GROUP BY u. role")
    List<Object[]> countUsersByRole();

    // ========== UPDATE ==========

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, 
                             @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.isBanned = :banned, u.banReason = :reason WHERE u.id = :userId")
    void updateBanStatus(@Param("userId") Long userId, 
                         @Param("banned") Boolean banned, 
                         @Param("reason") String reason);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, @Param("password") String password);

    @Modifying
    @Query("UPDATE User u SET u.emailVerifiedAt = :verifiedAt WHERE u.id = :userId")
    void updateEmailVerified(@Param("userId") Long userId, 
                             @Param("verifiedAt") LocalDateTime verifiedAt);
}