package com.argaty.service.impl;

import com.argaty.entity.PasswordResetToken;
import com.argaty.entity.User;
import com.argaty.enums.Role;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.exception.BadRequestException;
import com.argaty.repository.PasswordResetTokenRepository;
import com.argaty.repository.UserRepository;
import com.argaty.service.EmailService;
import com.argaty.service.NotificationService;
import com.argaty.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // ========== CRUD ==========

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // ========== AUTHENTICATION ==========

    @Override
    public User register(String fullName, String email, String password, String phone) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Kiểm tra phone đã tồn tại
        if (phone != null && !phone.isEmpty() && userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Số điện thoại đã được sử dụng");
        }

        // Tạo user mới
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .role(Role.USER)
                .isEnabled(true)
                .isBanned(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {}", email);

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        userRepository.updatePassword(userId, passwordEncoder.encode(newPassword));
        log.info("Updated password for user: {}", userId);
        
        // Send notification to user about password change
        try {
            notificationService.sendSystemNotification(
                userId,
                "Mật khẩu đã được thay đổi",
                "Mật khẩu của bạn đã được thay đổi thành công. Nếu bạn không thực hiện thao tác này, vui lòng liên hệ với chúng tôi ngay."
            );
        } catch (Exception e) {
            log.error("Failed to send password change notification: {}", e.getMessage());
        }
    }

    @Override
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLoginTime(userId, LocalDateTime.now());
    }

    // ========== PASSWORD RESET ==========

    @Override
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Xóa token cũ nếu có
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // Tạo token mới (30 phút hết hạn)
        PasswordResetToken token = PasswordResetToken.create(user, 30);
        passwordResetTokenRepository.save(token);

        // Gửi email
        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());

        log.info("Created password reset token for user: {}", email);
        return token.getToken();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        return passwordResetTokenRepository.findValidToken(token).isPresent();
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(token)
                .orElseThrow(() -> new BadRequestException("Token không hợp lệ hoặc đã hết hạn"));

        // Cập nhật mật khẩu
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đánh dấu token đã sử dụng
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        log.info("Reset password for user: {}", user.getEmail());
    }

    // ========== USER MANAGEMENT ==========

    @Override
    @Transactional(readOnly = true)
    public Page<User> findByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    @Override
    public void updateRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRole(role);
        userRepository.save(user);
        log.info("Updated role to {} for user: {}", role, userId);
    }

    @Override
    public void banUser(Long userId, String reason) {
        userRepository.updateBanStatus(userId, true, reason);
        log.info("Banned user: {} - Reason: {}", userId, reason);
    }

    @Override
    public void unbanUser(Long userId) {
        userRepository.updateBanStatus(userId, false, null);
        log.info("Unbanned user: {}", userId);
    }

    @Override
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsEnabled(false);
        userRepository.save(user);
    }

    // ========== PROFILE ==========

    @Override
    public User updateProfile(Long userId, String fullName, String phone, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            // Kiểm tra phone trùng với user khác
            if (!phone.isEmpty() && !phone.equals(user.getPhone())) {
                if (userRepository.existsByPhone(phone)) {
                    throw new BadRequestException("Số điện thoại đã được sử dụng");
                }
            }
            user.setPhone(phone);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        return userRepository.save(user);
    }

    @Override
    public User updateAddress(Long userId, String address, String city, String district, String ward) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setAddress(address);
        user.setCity(city);
        user.setDistrict(district);
        user.setWard(ward);

        return userRepository.save(user);
    }

    // ========== STATISTICS ==========

    @Override
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public long countNewUsersToday() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        return userRepository.countNewUsersFromDate(startOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public long countNewUsersThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        return userRepository.countNewUsersFromDate(startOfMonth);
    }
}