package com.argaty.controller.user;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.request.ChangePasswordRequest;
import com.argaty.dto.request.UpdateProfileRequest;
import com.argaty.entity.Notification;
import com.argaty.entity.Order;
import com.argaty.entity.Review;
import com.argaty.entity.User;
import com.argaty.entity.UserAddress;
import com.argaty.entity.Wishlist;
import com.argaty.enums.OrderStatus;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.exception.UnauthorizedException;
import com.argaty.service.FileStorageService;
import com.argaty.service.NotificationService;
import com.argaty.service.OrderService;
import com.argaty.service.ReviewService;
import com.argaty.service.UserAddressService;
import com.argaty.service.UserService;
import com.argaty.service.WishlistService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    private final FileStorageService fileStorageService;

    private User getCurrentUser(Principal principal) {
        if (principal == null) throw new UnauthorizedException("Vui lòng đăng nhập");
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getName()));
    }

    // --- 1. OVERVIEW ---
    @GetMapping
    public String profile(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        model.addAttribute("totalOrders", orderService.countByUserId(user.getId()));
        model.addAttribute("totalSpent", orderService.getTotalSpentByUser(user.getId()));
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        
        Page<Order> recentOrders = orderService.findByUserId(user.getId(), PageRequest.of(0, 5));
        model.addAttribute("recentOrders", DtoMapper.toOrderResponseList(recentOrders.getContent()));

        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "overview");
        return "user/profile/overview";
    }

    // --- 2. EDIT PROFILE ---
    @GetMapping("/edit")
    public String editProfile(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        
        // Map dữ liệu hiện tại vào form
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(user.getFullName());
        request.setPhone(user.getPhone());
        request.setAvatar(user.getAvatar());
        // ... map thêm các field khác nếu cần
        
        model.addAttribute("updateProfileRequest", request);
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "edit");
        return "user/profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute UpdateProfileRequest request,
                                BindingResult bindingResult,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = getCurrentUser(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", DtoMapper.toUserResponse(user)); // Reload user info
            model.addAttribute("currentPage", "profile");
            model.addAttribute("profileTab", "edit");
            return "user/profile/edit";
        }
        try {
            String avatarPath = request.getAvatar();
            if (avatarFile != null && !avatarFile.isEmpty()) {
                avatarPath = fileStorageService.uploadFile(avatarFile, "avatars/");
            }

            userService.updateProfile(user.getId(), request.getFullName(), request.getPhone(), avatarPath);
            // Xử lý update address riêng hoặc gộp tùy logic service của bạn
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/edit";
    }

    // --- 3. CHANGE PASSWORD (Giữ nguyên logic của bạn) ---
    @GetMapping("/change-password")
    public String changePasswordPage(Principal principal, Model model) {
        User user = getCurrentUser(principal); // Lấy user để hiển thị avatar bên sidebar
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "password");
        return "user/profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordRequest request,
                                 BindingResult bindingResult, Principal principal, RedirectAttributes redirectAttributes, Model model) {
        User user = getCurrentUser(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", DtoMapper.toUserResponse(user));
            model.addAttribute("currentPage", "profile");
            model.addAttribute("profileTab", "password");
            return "user/profile/change-password";
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/profile/change-password";
        }
        if (!userService.checkPassword(user, request.getCurrentPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/profile/change-password";
        }
        userService.updatePassword(user.getId(), request.getNewPassword());
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/profile/change-password";
    }

    // --- 4. ORDERS (Giữ nguyên logic của bạn) ---
    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "0") int page,
                         Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        
        Page<Order> orders;
        // Logic lọc order...
        orders = orderService.findByUserId(user.getId(), PageRequest.of(page, 10, Sort.by("createdAt").descending()));
        
        model.addAttribute("orders", DtoMapper.toOrderPageResponse(orders));
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "orders");
        return "user/profile/orders";
    }

    @GetMapping("/orders/{orderCode}")
    public String orderDetail(@PathVariable String orderCode, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderCode", orderCode));
        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "orders");
        return "user/profile/order-detail";
    }
    
    // Cancel Order POST (Giữ nguyên)

    // --- 5. ADDRESSES (Bổ sung Mapping) ---
    @GetMapping("/addresses")
    public String addresses(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        
        List<UserAddress> addresses = userAddressService.findByUserId(user.getId());
        model.addAttribute("addresses", DtoMapper.toUserAddressResponseList(addresses));
        
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "addresses");
        return "user/profile/addresses"; // Tên file HTML của bạn là address.html
    }

    // --- 6. WISHLIST (Bổ sung Mapping) ---
    @GetMapping("/wishlist")
    public String wishlist(@RequestParam(defaultValue = "0") int page, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        
        // Giả sử service có hàm trả về Page<Wishlist> hoặc List<Wishlist>
        List<Wishlist> wishlists = wishlistService.findByUserIdWithProduct(user.getId());
        model.addAttribute("wishlists", DtoMapper.toWishlistResponseList(wishlists));
        
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "wishlist");
        return "user/profile/wishlist";
    }

    // --- 7. REVIEWS (Bổ sung Mapping) ---
    @GetMapping("/reviews")
    public String reviews(@RequestParam(defaultValue = "0") int page, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        
        Page<Review> reviews = reviewService.findByUserId(user.getId(), PageRequest.of(page, 10, Sort.by("createdAt").descending()));
        model.addAttribute("reviews", DtoMapper.toReviewPageResponse(reviews));
        
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "reviews");
        return "user/profile/reviews";
    }

    // --- 8. NOTIFICATIONS (Bổ sung Mapping) ---
    @GetMapping("/notifications")
    public String notifications(@RequestParam(defaultValue = "0") int page, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", DtoMapper.toUserResponse(user));
        
        Page<Notification> notifications = notificationService.findByUserId(user.getId(), PageRequest.of(page, 20, Sort.by("createdAt").descending()));
        model.addAttribute("notifications", DtoMapper.toNotificationResponseList(notifications.getContent()));
        
        model.addAttribute("currentPage", "profile");
        model.addAttribute("profileTab", "notifications");
        return "user/profile/notifications";
    }
}