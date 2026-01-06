package com.argaty. controller.user;

import com. argaty.dto.request. ChangePasswordRequest;
import com. argaty.dto.request.UpdateProfileRequest;
import com.argaty.dto.response.*;
import com.argaty.entity.*;
import com.argaty.enums.OrderStatus;
import com.argaty.exception.BadRequestException;
import com.argaty.service.*;
import com. argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org. springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework. data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework. web.bind.annotation.*;
import org.springframework.web.servlet. mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

/**
 * Controller cho trang cá nhân
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final NotificationService notificationService;
    private final UserAddressService userAddressService;

    /**
     * Lấy user hiện tại
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new com.argaty.exception.UnauthorizedException("Vui lòng đăng nhập");
        }
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));
    }

    /**
     * Trang profile chính
     */
    @GetMapping
    public String profile(Principal principal, Model model) {
        User user = getCurrentUser(principal);

        model.addAttribute("user", DtoMapper.toUserResponse(user));

        // Thống kê đơn hàng
        model.addAttribute("totalOrders", orderService.countByUserId(user.getId()));
        model.addAttribute("totalSpent", orderService.getTotalSpentByUser(user.getId()));
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));

        // Recent orders
        Page<Order> recentOrders = orderService.findByUserId(user.getId(), PageRequest.of(0, 5));
        model.addAttribute("recentOrders", DtoMapper.toOrderResponseList(recentOrders. getContent()));

        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "overview");
        return "user/profile/overview";
    }

    /**
     * Chỉnh sửa thông tin cá nhân
     */
    @GetMapping("/edit")
    public String editProfile(Principal principal, Model model) {
        User user = getCurrentUser(principal);

        model.addAttribute("user", DtoMapper.toUserResponse(user));
        model.addAttribute("updateProfileRequest", new UpdateProfileRequest());
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "edit");
        return "user/profile/edit";
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @PostMapping("/edit")
    public String updateProfile(
            @Valid @ModelAttribute UpdateProfileRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);

        if (bindingResult.hasErrors()) {
            return "user/profile/edit";
        }

        try {
            userService.updateProfile(user.getId(), request.getFullName(), 
                    request.getPhone(), request.getAvatar());

            if (request.getAddress() != null) {
                userService.updateAddress(user.getId(), request.getAddress(),
                        request.getCity(), request.getDistrict(), request.getWard());
            }

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/edit";
    }

    /**
     * Trang đổi mật khẩu
     */
    @GetMapping("/change-password")
    public String changePasswordPage(Principal principal, Model model) {
        getCurrentUser(principal);
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "password");
        return "user/profile/change-password";
    }

    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute ChangePasswordRequest request,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        User user = getCurrentUser(principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPage", "profile");
            model.addAttribute("profileTab", "password");
            return "user/profile/change-password";
        }

        // Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/profile/change-password";
        }

        // Check current password
        if (!userService. checkPassword(user, request.getCurrentPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/profile/change-password";
        }

        userService.updatePassword(user.getId(), request.getNewPassword());
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/profile/change-password";
    }

    /**
     * Danh sách đơn hàng
     */
    @GetMapping("/orders")
    public String orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {

        User user = getCurrentUser(principal);

        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status. toUpperCase());
                orders = orderService.findByUserId(user.getId(), PageRequest.of(page, 10));
                // Filter by status in memory (hoặc tạo thêm method trong service)
            } catch (IllegalArgumentException e) {
                orders = orderService.findByUserId(user.getId(), PageRequest.of(page, 10));
            }
        } else {
            orders = orderService.findByUserId(user.getId(), PageRequest.of(page, 10));
        }

        model. addAttribute("orders", DtoMapper.toOrderPageResponse(orders));
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "orders");
        return "user/profile/orders";
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/orders/{orderCode}")
    public String orderDetail(@PathVariable String orderCode, Principal principal, Model model) {
        User user = getCurrentUser(principal);

        Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new com.argaty.exception. ResourceNotFoundException("Order", "orderCode", orderCode));

        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "orders");
        return "user/profile/order-detail";
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/orders/{orderCode}/cancel")
    public String cancelOrder(
            @PathVariable String orderCode,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);

        try {
            Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                    . orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "orderCode", orderCode));

            orderService.cancelOrder(order.getId(), user, reason);
            redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/orders/" + orderCode;
    }

    /**
     * Danh sách địa chỉ
     */
    @GetMapping("/addresses")
    public String addresses(Principal principal, Model model) {
        User user = getCurrentUser(principal);

        List<UserAddress> addresses = userAddressService.findByUserId(user.getId());
        model.addAttribute("addresses", DtoMapper.toUserAddressResponseList(addresses));
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "addresses");
        return "user/profile/addresses";
    }

    /**
     * Danh sách yêu thích
     */
    @GetMapping("/wishlist")
    public String wishlist(
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {

        User user = getCurrentUser(principal);

        List<Wishlist> wishlists = wishlistService.findByUserIdWithProduct(user.getId());
        model.addAttribute("wishlists", DtoMapper.toWishlistResponseList(wishlists));
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "wishlist");
        return "user/profile/wishlist";
    }

    /**
     * Danh sách đánh giá
     */
    @GetMapping("/reviews")
    public String reviews(
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {

        User user = getCurrentUser(principal);

        Page<Review> reviews = reviewService.findByUserId(user.getId(), PageRequest.of(page, 10));
        model.addAttribute("reviews", DtoMapper.toReviewPageResponse(reviews));
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "reviews");
        return "user/profile/reviews";
    }

    /**
     * Thông báo
     */
    @GetMapping("/notifications")
    public String notifications(
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {

        User user = getCurrentUser(principal);

        Page<Notification> notifications = notificationService. findByUserId(user.getId(), PageRequest.of(page, 20));
        model.addAttribute("notifications", DtoMapper.toNotificationResponseList(notifications. getContent()));
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "notifications");
        return "user/profile/notifications";
    }
}