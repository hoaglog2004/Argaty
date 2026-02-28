package com.argaty.controller.auth;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.request.LoginRequest;
import com.argaty.dto.request.PasswordResetRequest;
import com.argaty.dto.request.RegisterRequest;
import com.argaty.entity.User;
import com.argaty.exception.BadRequestException;
import com.argaty.service.CartService;
import com.argaty.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller cho authentication
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CartService cartService;

    /**
     * Trang đăng nhập
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Principal principal,
            Model model) {

        // Nếu đã đăng nhập, redirect về home
        if (principal != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
        }
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công");
        }

        model.addAttribute("redirect", redirect);
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("authPage", "login");
        return "auth/login";
    }

    /**
     * Trang đăng ký
     */
    @GetMapping("/register")
    public String registerPage(Principal principal, Model model) {
        if (principal != null) {
            return "redirect:/";
        }

        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("authPage", "register");
        return "auth/register";
    }

    /**
     * Xử lý đăng ký
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("authPage", "register");
            return "auth/register";
        }

        // Validate confirm password
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            model.addAttribute("authPage", "register");
            return "auth/register";
        }

        // Validate agree terms
        if (request.getAgreeTerms() == null || !request.getAgreeTerms()) {
            model.addAttribute("error", "Vui lòng đồng ý với điều khoản sử dụng");
            model.addAttribute("authPage", "register");
            return "auth/register";
        }

        try {
            User user = userService.register(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getPhone()
            );

            // Merge guest cart nếu có
            String sessionId = (String) session.getAttribute("CART_SESSION_ID");
            if (sessionId != null) {
                cartService.mergeGuestCartToUser(sessionId, user.getId());
                session.removeAttribute("CART_SESSION_ID");
            }

            redirectAttributes.addFlashAttribute("success", 
                    "Đăng ký thành công!  Vui lòng đăng nhập.");
            return "redirect:/auth/login";

        } catch (BadRequestException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("authPage", "register");
            return "auth/register";
        }
    }

    /**
     * Trang quên mật khẩu
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("passwordResetRequest", new PasswordResetRequest());
        model.addAttribute("authPage", "forgot");
        return "auth/forgot-password";
    }

    /**
     * Xử lý gửi email reset password
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute PasswordResetRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("authPage", "forgot");
            return "auth/forgot-password";
        }

        try {
            userService.createPasswordResetToken(request.getEmail());
            redirectAttributes.addFlashAttribute("success", 
                    "Đã gửi email hướng dẫn đặt lại mật khẩu.Vui lòng kiểm tra hộp thư.");
            return "redirect:/auth/forgot-password";
        } catch (Exception e) {
            // Không tiết lộ email có tồn tại hay không
            redirectAttributes.addFlashAttribute("success", 
                    "Nếu email tồn tại, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu.");
            return "redirect:/auth/forgot-password";
        }
    }

    /**
     * Trang đặt lại mật khẩu
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validate token
        if (!userService.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("error", 
                    "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/auth/forgot-password";
        }

        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken(token);
        model.addAttribute("passwordResetRequest", request);
        model.addAttribute("authPage", "reset");
        return "auth/reset-password";
    }

    /**
     * Xử lý đặt lại mật khẩu
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute PasswordResetRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("authPage", "reset");
            return "auth/reset-password";
        }

        // Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            model.addAttribute("authPage", "reset");
            return "auth/reset-password";
        }

        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("success", 
                    "Đặt lại mật khẩu thành công!  Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }
}