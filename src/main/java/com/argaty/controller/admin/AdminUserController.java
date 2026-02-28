package com.argaty.controller.admin;

import com.argaty.dto.response.UserResponse;
import com.argaty.entity.User;
import com.argaty.enums.Role;
import com.argaty.service.OrderService;
import com.argaty.service.UserService;
import com.argaty.util.DtoMapper;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý người dùng (Admin)
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users;
        if (q != null && !q.trim().isEmpty()) {
            users = userService.searchUsers(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else if (role != null && !role.isEmpty()) {
            try {
                Role userRole = Role.valueOf(role.toUpperCase());
                users = userService.findByRole(userRole, pageRequest);
                model.addAttribute("selectedRole", role);
            } catch (IllegalArgumentException e) {
                users = userService.findAll(pageRequest);
            }
        } else {
            users = userService.findAll(pageRequest);
        }

        model.addAttribute("users", DtoMapper.toUserPageResponse(users));
        model.addAttribute("roles", Role.values());
        model.addAttribute("adminPage", "users");

        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "id", id));

        UserResponse userResponse = UserResponse.fromEntity(user);
        userResponse.setOrderCount(orderService.countByUserId(id));
        userResponse.setTotalSpent(orderService.getTotalSpentByUser(id).longValue());

        model.addAttribute("user", userResponse);
        model.addAttribute("roles", Role.values());
        model.addAttribute("adminPage", "users");

        return "admin/users/detail";
    }

    @PostMapping("/{id}/role")
    public String updateRole(
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            userService.updateRole(id, newRole);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật vai trò");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/ban")
    public String banUser(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {

        userService.banUser(id, reason);
        redirectAttributes.addFlashAttribute("success", "Đã khóa tài khoản");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.unbanUser(id);
        redirectAttributes.addFlashAttribute("success", "Đã mở khóa tài khoản");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "id", id));
        if (user.getIsEnabled()) {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("success", "Đã vô hiệu hóa tài khoản");
        } else {
            userService.enableUser(id);
            redirectAttributes.addFlashAttribute("success", "Đã kích hoạt tài khoản");
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tài khoản người dùng");
        } catch (DataIntegrityViolationException e) {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("error", "Tài khoản đang có dữ liệu liên quan, đã chuyển sang vô hiệu hóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}