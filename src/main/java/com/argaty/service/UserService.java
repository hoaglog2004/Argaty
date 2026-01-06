package com.argaty.service;

import com.argaty.entity.User;
import com.argaty.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho User
 */
public interface UserService {

    // ========== CRUD ==========
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    List<User> findAll();
    
    Page<User> findAll(Pageable pageable);
    
    void deleteById(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);

    // ========== AUTHENTICATION ==========
    
    User register(String fullName, String email, String password, String phone);
    
    boolean checkPassword(User user, String rawPassword);
    
    void updatePassword(Long userId, String newPassword);
    
    void updateLastLogin(Long userId);

    // ========== PASSWORD RESET ==========
    
    String createPasswordResetToken(String email);
    
    boolean validatePasswordResetToken(String token);
    
    void resetPassword(String token, String newPassword);

    // ========== USER MANAGEMENT ==========
    
    Page<User> findByRole(Role role, Pageable pageable);
    
    Page<User> searchUsers(String keyword, Pageable pageable);
    
    void updateRole(Long userId, Role role);
    
    void banUser(Long userId, String reason);
    
    void unbanUser(Long userId);
    
    void enableUser(Long userId);
    
    void disableUser(Long userId);

    // ========== PROFILE ==========
    
    User updateProfile(Long userId, String fullName, String phone, String avatar);
    
    User updateAddress(Long userId, String address, String city, String district, String ward);

    // ========== STATISTICS ==========
    
    long countByRole(Role role);
    
    long countNewUsersToday();
    
    long countNewUsersThisMonth();
}