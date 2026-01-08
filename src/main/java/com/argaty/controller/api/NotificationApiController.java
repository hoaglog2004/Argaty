package com.argaty.controller.api;

import com.argaty.dto.response.ApiResponse;
import com.argaty.dto.response.NotificationResponse;
import com.argaty.entity.Notification;
import com.argaty.entity.User;
import com.argaty.service.NotificationService;
import com.argaty.service.UserService;
import com.argaty.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho thông báo
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Lấy danh sách thông báo gần đây
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(Principal principal) {
        User user = getCurrentUser(principal);
        List<Notification> notifications = notificationService.findRecentByUserId(user.getId(), 10);
        return ResponseEntity.ok(ApiResponse.success(DtoMapper.toNotificationResponseList(notifications)));
    }

    /**
     * Lấy thông báo chưa đọc
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(Principal principal) {
        User user = getCurrentUser(principal);
        List<Notification> notifications = notificationService.findUnreadByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(DtoMapper.toNotificationResponseList(notifications)));
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(Principal principal) {
        User user = getCurrentUser(principal);
        int count = notificationService.countUnreadByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Lấy thông báo + số chưa đọc (cho header)
     */
    @GetMapping("/header")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeaderNotifications(Principal principal) {
        User user = getCurrentUser(principal);

        List<Notification> notifications = notificationService.findRecentByUserId(user.getId(), 5);
        int unreadCount = notificationService.countUnreadByUserId(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("notifications", DtoMapper.toNotificationResponseList(notifications));
        data.put("unreadCount", unreadCount);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Đánh dấu đã đọc một thông báo
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, Principal principal) {
        getCurrentUser(principal); // Verify user
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc"));
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Principal principal) {
        User user = getCurrentUser(principal);
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả đã đọc"));
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new com.argaty.exception.UnauthorizedException("Vui lòng đăng nhập");
        }
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));
    }
}