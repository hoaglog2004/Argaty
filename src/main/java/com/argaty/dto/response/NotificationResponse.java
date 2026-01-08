package com.argaty.dto.response;

import com.argaty.entity.Notification;
import com.argaty.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response thông báo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private String typeIcon;
    private String link;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String timeAgo;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .typeIcon(notification.getType().getIcon())
                .link(notification.getLink())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .timeAgo(calculateTimeAgo(notification.getCreatedAt()))
                .build();
    }

    private static String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";

        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";

        long days = hours / 24;
        if (days < 7) return days + " ngày trước";

        long weeks = days / 7;
        if (weeks < 4) return weeks + " tuần trước";

        long months = days / 30;
        if (months < 12) return months + " tháng trước";

        return (days / 365) + " năm trước";
    }
}