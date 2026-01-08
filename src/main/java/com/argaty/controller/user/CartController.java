package com.argaty.controller.user;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.argaty.dto.response.CartResponse;
import com.argaty.entity.Cart;
import com.argaty.entity.User;
import com.argaty.entity.Voucher;
import com.argaty.service.CartService;
import com.argaty.service.UserService;
import com.argaty.service.VoucherService;
import com.argaty.util.DtoMapper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controller cho giỏ hàng
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final VoucherService voucherService;

    /**
     * Trang giỏ hàng
     */
    @GetMapping
    public String cart(Principal principal, HttpSession session, Model model) {
        Cart cart;

        if (principal != null) {
            // User đã đăng nhập
            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));
            cart = cartService.getOrCreateCart(user.getId());

            // Lấy vouchers khả dụng
            BigDecimal cartTotal = cart.getTotalAmount();
            List<Voucher> availableVouchers = voucherService.findVouchersForUser(user.getId(), cartTotal);
            model.addAttribute("availableVouchers", DtoMapper.toVoucherResponseList(availableVouchers));
        } else {
            // Guest user - dùng session
            String sessionId = getOrCreateSessionId(session);
            cart = cartService.getOrCreateCartBySession(sessionId);
        }

        CartResponse cartResponse = DtoMapper.toCartResponse(cart);
        model.addAttribute("cart", cartResponse);

        // Tính phí ship
        BigDecimal shippingFee = calculateShippingFee(cart.getTotalAmount());
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("freeShippingThreshold", BigDecimal.valueOf(500000));

        model.addAttribute("currentPage", "cart");
        return "user/cart";
    }

    /**
     * Lấy hoặc tạo session ID cho guest
     */
    private String getOrCreateSessionId(HttpSession session) {
        String sessionId = (String) session.getAttribute("CART_SESSION_ID");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("CART_SESSION_ID", sessionId);
        }
        return sessionId;
    }

    /**
     * Tính phí ship
     */
    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(30000);
    }
}