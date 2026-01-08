package com.argaty.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Notification;
import com.argaty.enums.NotificationType;

/**
 * Repository cho Notification Entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable);

    int countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.createdAt < :before")
    void deleteOldNotifications(@Param("userId") Long userId, @Param("before") LocalDateTime before);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.isRead = true")
    void deleteReadNotifications(@Param("userId") Long userId);
}