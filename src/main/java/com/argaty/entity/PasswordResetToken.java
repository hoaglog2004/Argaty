package com.argaty.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa. domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta. persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity PasswordResetToken - Token reset mật khẩu
 */
@Entity
@Table(name = "password_reset_tokens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType. LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (token == null) {
            token = generateToken();
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Tạo token ngẫu nhiên
     */
    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Kiểm tra token còn hiệu lực
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Kiểm tra token đã hết hạn
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Đánh dấu token đã sử dụng
     */
    public void markAsUsed() {
        this.used = true;
    }

    /**
     * Tạo PasswordResetToken mới
     * @param user User cần reset password
     * @param expiryMinutes Số phút token có hiệu lực
     */
    public static PasswordResetToken create(User user, int expiryMinutes) {
        return PasswordResetToken.builder()
                .user(user)
                .token(generateToken())
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();
    }
}