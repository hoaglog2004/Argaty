package com.argaty. service;

import com.argaty.entity.Notification;
import com.argaty.entity.Order;
import com.argaty. enums.NotificationType;
import com.argaty.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface cho Notification
 */
public interface NotificationService {

    Notification save(Notification notification);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    List<Notification> findUnreadByUserId(Long userId);

    List<Notification> findRecentByUserId(Long userId, int limit);

    int countUnreadByUserId(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    void deleteOldNotifications(Long userId, int daysOld);

    // ========== SEND NOTIFICATIONS ==========

    void sendNotification(Long userId, String title, String message, NotificationType type, String link);

    void sendOrderCreatedNotification(Order order);

    void sendOrderStatusNotification(Order order, OrderStatus oldStatus, OrderStatus newStatus);

    void sendPromotionNotification(List<Long> userIds, String title, String message, String link);

    void sendSystemNotification(Long userId, String title, String message);
}