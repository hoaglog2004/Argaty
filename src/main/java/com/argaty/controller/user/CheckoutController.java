package com.argaty.controller.user;

import com.argaty.dto.request.CheckoutRequest;
import com.argaty.dto.response.*;
import com.argaty.entity.*;
import com.argaty.enums.PaymentMethod;
import com.argaty.exception.BadRequestException;
import com.argaty.service.*;
import com. argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui. Model;
import org.springframework. validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util. List;

/**
 * Controller cho thanh toán
 */
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final UserAddressService userAddressService;
    private final VoucherService voucherService;

    /**
     * Trang thanh toán
     */
    @GetMapping
    public String checkout(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login?redirect=/checkout";
        }

        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        // Lấy giỏ hàng
        Cart cart = cartService.findByUserIdWithItems(user.getId());
        if (cart. isEmpty() || cart.getSelectedItemCount() == 0) {
            return "redirect:/cart? error=empty";
        }

        // Validate cart items
        if (!cartService.validateCartItems(cart. getId())) {
            return "redirect:/cart?error=invalid";
        }

        CartResponse cartResponse = DtoMapper. toCartResponse(cart);
        model.addAttribute("cart", cartResponse);

        // User addresses
        List<UserAddress> addresses = userAddressService.findByUserId(user.getId());
        model.addAttribute("addresses", DtoMapper.toUserAddressResponseList(addresses));

        // Default address
        UserAddress defaultAddress = userAddressService.findDefaultAddress(user.getId()).orElse(null);
        model.addAttribute("defaultAddress", defaultAddress != null ? 
                DtoMapper.toUserAddressResponse(defaultAddress) : null);

        // Available vouchers
        BigDecimal cartTotal = cart.getTotalAmount();
        List<Voucher> vouchers = voucherService.findVouchersForUser(user. getId(), cartTotal);
        model.addAttribute("availableVouchers", DtoMapper.toVoucherResponseList(vouchers));

        // Payment methods
        model.addAttribute("paymentMethods", PaymentMethod.values());

        // Tính phí ship
        BigDecimal shippingFee = calculateShippingFee(cartTotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("subtotal", cartTotal);
        model.addAttribute("totalAmount", cartTotal.add(shippingFee));

        // User info
        model.addAttribute("user", DtoMapper.toUserResponse(user));

        // Checkout form
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        if (defaultAddress != null) {
            checkoutRequest.setReceiverName(defaultAddress.getReceiverName());
            checkoutRequest.setReceiverPhone(defaultAddress.getPhone());
            checkoutRequest.setShippingAddress(defaultAddress.getAddress());
            checkoutRequest. setCity(defaultAddress.getCity());
            checkoutRequest.setDistrict(defaultAddress.getDistrict());
            checkoutRequest.setWard(defaultAddress.getWard());
        } else {
            checkoutRequest. setReceiverName(user. getFullName());
            checkoutRequest. setReceiverPhone(user. getPhone());
        }
        checkoutRequest.setReceiverEmail(user.getEmail());
        model.addAttribute("checkoutRequest", checkoutRequest);

        model.addAttribute("currentPage", "checkout");
        return "user/checkout";
    }

    /**
     * Xử lý đặt hàng
     */
    @PostMapping
    public String placeOrder(
            @Valid @ModelAttribute CheckoutRequest checkoutRequest,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (principal == null) {
            return "redirect:/auth/login?redirect=/checkout";
        }

        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        if (bindingResult.hasErrors()) {
            // Reload checkout page với errors
            return reloadCheckoutPage(user, model, checkoutRequest);
        }

        try {
            Order order = orderService.createOrder(
                    user.getId(),
                    checkoutRequest.getReceiverName(),
                    checkoutRequest.getReceiverPhone(),
                    checkoutRequest.getReceiverEmail(),
                    checkoutRequest.getShippingAddress(),
                    checkoutRequest.getCity(),
                    checkoutRequest.getDistrict(),
                    checkoutRequest.getWard(),
                    checkoutRequest.getPaymentMethod(),
                    checkoutRequest.getVoucherCode(),
                    checkoutRequest. getNote()
            );

            // Redirect theo payment method
            if (order.getPaymentMethod() == PaymentMethod.COD) {
                return "redirect:/checkout/success?orderCode=" + order.getOrderCode();
            } else {
                // Redirect to payment gateway
                return "redirect:/checkout/payment?orderCode=" + order.getOrderCode();
            }

        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Trang thanh toán online
     */
    @GetMapping("/payment")
    public String payment(@RequestParam String orderCode, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "orderCode", orderCode));

        if (order.getIsPaid()) {
            return "redirect:/checkout/success?orderCode=" + orderCode;
        }

        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        return "user/payment";
    }

    /**
     * Trang đặt hàng thành công
     */
    @GetMapping("/success")
    public String orderSuccess(@RequestParam String orderCode, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "orderCode", orderCode));

        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        return "user/order-success";
    }

    /**
     * Reload checkout page khi có lỗi
     */
    private String reloadCheckoutPage(User user, Model model, CheckoutRequest checkoutRequest) {
        Cart cart = cartService.findByUserIdWithItems(user.getId());
        model.addAttribute("cart", DtoMapper.toCartResponse(cart));
        model.addAttribute("addresses", DtoMapper.toUserAddressResponseList(
                userAddressService.findByUserId(user.getId())));
        model.addAttribute("paymentMethods", PaymentMethod. values());
        model.addAttribute("checkoutRequest", checkoutRequest);

        BigDecimal cartTotal = cart.getTotalAmount();
        BigDecimal shippingFee = calculateShippingFee(cartTotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("subtotal", cartTotal);
        model.addAttribute("totalAmount", cartTotal.add(shippingFee));

        return "user/checkout";
    }

    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        if (subtotal. compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal. ZERO;
        }
        return BigDecimal.valueOf(30000);
    }
}