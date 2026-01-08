package com.argaty.service.impl;

import com.argaty.entity.Notification;
import com.argaty.entity.Order;
import com.argaty.entity.User;
import com.argaty.enums.NotificationType;
import com.argaty.enums.OrderStatus;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.NotificationRepository;
import com.argaty.repository.UserRepository;
import com.argaty.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation của NotificationService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findRecentByUserId(Long userId, int limit) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUnreadByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
        log.info("Marked all notifications as read for user {}", userId);
    }

    @Override
    public void deleteOldNotifications(Long userId, int daysOld) {
        LocalDateTime before = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldNotifications(userId, before);
        log.info("Deleted old notifications for user {} (older than {} days)", userId, daysOld);
    }

    // ========== SEND NOTIFICATIONS ==========

    @Override
    public void sendNotification(Long userId, String title, String message, NotificationType type, String link) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Sent notification to user {}: {}", userId, title);
    }

        @Override
    public void sendOrderCreatedNotification(Order order) {
        String title = "Đặt hàng thành công";
        String message = String.format("Đơn hàng #%s đã được tạo.  Tổng tiền: %,d VNĐ",
                order.getOrderCode(), order.getTotalAmount().longValue());
        String link = "/profile/orders/" + order.getOrderCode();

        sendNotification(order.getUser().getId(), title, message, NotificationType.ORDER, link);
    }

    @Override
    public void sendOrderStatusNotification(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        String title = "Cập nhật đơn hàng";
        String message = String.format("Đơn hàng #%s:  %s",
                order.getOrderCode(), newStatus.getDisplayName());
        String link = "/profile/orders/" + order.getOrderCode();

        sendNotification(order.getUser().getId(), title, message, NotificationType.ORDER, link);
    }

    @Override
    public void sendPromotionNotification(List<Long> userIds, String title, String message, String link) {
        for (Long userId : userIds) {
            try {
                sendNotification(userId, title, message, NotificationType.PROMOTION, link);
            } catch (Exception e) {
                log.error("Failed to send promotion notification to user {}: {}", userId, e.getMessage());
            }
        }
        log.info("Sent promotion notification to {} users", userIds.size());
    }

    @Override
    public void sendSystemNotification(Long userId, String title, String message) {
        sendNotification(userId, title, message, NotificationType.SYSTEM, null);
    }
}