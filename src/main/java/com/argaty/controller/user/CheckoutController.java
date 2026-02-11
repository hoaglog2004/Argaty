package com.argaty.controller.user;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.request.CheckoutRequest;
import com.argaty.dto.response.CartResponse;
import com.argaty.entity.Cart;
import com.argaty.entity.Order;
import com.argaty.entity.User;
import com.argaty.entity.UserAddress;
import com.argaty.entity.Voucher;
import com.argaty.enums.PaymentMethod;
import com.argaty.exception.BadRequestException;
import com.argaty.service.CartService;
import com.argaty.service.OrderService;
import com.argaty.service.UserAddressService;
import com.argaty.service.UserService;
import com.argaty.service.VoucherService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final UserAddressService userAddressService;
    private final VoucherService voucherService;

    // --- 1. TRANG THANH TOÁN ---
    @GetMapping
    public String checkout(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login?redirect=/checkout";
        }

        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        Cart cart = cartService.findByUserIdWithItems(user.getId());
        if (cart.isEmpty() || cart.getSelectedItemCount() == 0) {
            return "redirect:/cart?error=empty";
        }

        if (!cartService.validateCartItems(cart.getId())) {
            return "redirect:/cart?error=invalid";
        }

        CartResponse cartResponse = DtoMapper.toCartResponse(cart);
        model.addAttribute("cart", cartResponse);

        List<UserAddress> addresses = userAddressService.findByUserId(user.getId());
        model.addAttribute("addresses", DtoMapper.toUserAddressResponseList(addresses));

        UserAddress defaultAddress = userAddressService.findDefaultAddress(user.getId()).orElse(null);
        model.addAttribute("defaultAddress", defaultAddress != null ? DtoMapper.toUserAddressResponse(defaultAddress) : null);

        BigDecimal cartTotal = cart.getTotalAmount();
        List<Voucher> vouchers = voucherService.findVouchersForUser(user.getId(), cartTotal);
        model.addAttribute("availableVouchers", DtoMapper.toVoucherResponseList(vouchers));
        model.addAttribute("paymentMethods", PaymentMethod.values());

        BigDecimal shippingFee = calculateShippingFee(cartTotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("subtotal", cartTotal);
        model.addAttribute("totalAmount", cartTotal.add(shippingFee));
        model.addAttribute("user", DtoMapper.toUserResponse(user));

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        if (defaultAddress != null) {
            checkoutRequest.setReceiverName(defaultAddress.getReceiverName());
            checkoutRequest.setReceiverPhone(defaultAddress.getPhone());
            checkoutRequest.setShippingAddress(defaultAddress.getAddress());
            checkoutRequest.setCity(defaultAddress.getCity());
            checkoutRequest.setDistrict(defaultAddress.getDistrict());
            checkoutRequest.setWard(defaultAddress.getWard());
            // Quan trọng: Set addressId để radio button tự chọn
            checkoutRequest.setAddressId(defaultAddress.getId());
        } else {
            checkoutRequest.setReceiverName(user.getFullName());
            checkoutRequest.setReceiverPhone(user.getPhone());
        }
        checkoutRequest.setReceiverEmail(user.getEmail());
        model.addAttribute("checkoutRequest", checkoutRequest);

        model.addAttribute("currentPage", "checkout");
        return "user/checkout";
    }

    // --- 2. XỬ LÝ ĐẶT HÀNG (Đã sửa Logic Validate) ---
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

        // --- [QUAN TRỌNG] PHẢI KHAI BÁO USER TRƯỚC KHI DÙNG ---
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        // --- A. LOGIC GÁN ĐỊA CHỈ TỪ DB NẾU CHỌN ĐỊA CHỈ CÓ SẴN ---
        if (checkoutRequest.getAddressId() != null) {
            // Nếu người dùng chọn radio button (địa chỉ cũ)
            UserAddress savedAddr = userAddressService.findByIdAndUserId(checkoutRequest.getAddressId(), user.getId())
                    .orElse(null);
            
            if (savedAddr != null) {
                // Gán ngược lại dữ liệu từ DB vào request để tạo đơn hàng
                checkoutRequest.setReceiverName(savedAddr.getReceiverName());
                checkoutRequest.setReceiverPhone(savedAddr.getPhone());
                checkoutRequest.setShippingAddress(savedAddr.getAddress());
                checkoutRequest.setCity(savedAddr.getCity());
                checkoutRequest.setDistrict(savedAddr.getDistrict());
                checkoutRequest.setWard(savedAddr.getWard());
            } else {
                 bindingResult.rejectValue("addressId", "error.addressId", "Địa chỉ đã chọn không hợp lệ");
            }
        } else {
            // --- B. NẾU NHẬP ĐỊA CHỈ MỚI -> TỰ KIỂM TRA (VALIDATE THỦ CÔNG) ---
            if (isEmpty(checkoutRequest.getReceiverName())) {
                bindingResult.rejectValue("receiverName", "error.receiverName", "Tên người nhận không được để trống");
            }
            if (isEmpty(checkoutRequest.getReceiverPhone())) {
                bindingResult.rejectValue("receiverPhone", "error.receiverPhone", "SĐT không được để trống");
            }
            if (isEmpty(checkoutRequest.getCity())) {
                bindingResult.rejectValue("city", "error.city", "Vui lòng chọn Tỉnh/Thành phố");
            }
            if (isEmpty(checkoutRequest.getDistrict())) {
                bindingResult.rejectValue("district", "error.district", "Vui lòng chọn Quận/Huyện");
            }
            if (isEmpty(checkoutRequest.getShippingAddress())) {
                bindingResult.rejectValue("shippingAddress", "error.shippingAddress", "Địa chỉ chi tiết không được để trống");
            }
        }

        // --- C. KIỂM TRA LỖI SAU CÙNG ---
        if (bindingResult.hasErrors()) {
            return reloadCheckoutPage(user, model, checkoutRequest);
        }

        // --- D. TẠO ĐƠN HÀNG ---
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
                    checkoutRequest.getNote()
            );

            if (order.getPaymentMethod() == PaymentMethod.COD) {
                return "redirect:/checkout/success?orderCode=" + order.getOrderCode();
            } else {
                return "redirect:/checkout/payment?orderCode=" + order.getOrderCode();
            }

        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    // --- 3. CÁC HÀM PHỤ TRỢ ---

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @GetMapping("/payment")
    public String payment(@RequestParam String orderCode, Principal principal, Model model) {
        if (principal == null) return "redirect:/auth/login";
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "orderCode", orderCode));
        if (order.getIsPaid()) return "redirect:/checkout/success?orderCode=" + orderCode;
        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        return "user/payment";
    }

    @GetMapping("/success")
    public String orderSuccess(@RequestParam String orderCode, Principal principal, Model model) {
        if (principal == null) return "redirect:/auth/login";
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        Order order = orderService.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "orderCode", orderCode));
        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        return "user/order-success";
    }

    private String reloadCheckoutPage(User user, Model model, CheckoutRequest checkoutRequest) {
        Cart cart = cartService.findByUserIdWithItems(user.getId());
        model.addAttribute("cart", DtoMapper.toCartResponse(cart));
        model.addAttribute("addresses", DtoMapper.toUserAddressResponseList(userAddressService.findByUserId(user.getId())));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("checkoutRequest", checkoutRequest);

        BigDecimal cartTotal = cart.getTotalAmount();
        BigDecimal shippingFee = calculateShippingFee(cartTotal);
        BigDecimal discount = BigDecimal.ZERO;
        
        // --- RELOAD VOUCHER IF APPLIED ---
        if (checkoutRequest.getVoucherCode() != null && !checkoutRequest.getVoucherCode().isEmpty()) {
            try {
                 discount = voucherService.calculateDiscount(checkoutRequest.getVoucherCode(), cartTotal);
                 model.addAttribute("appliedVoucherCode", checkoutRequest.getVoucherCode());
                 model.addAttribute("discountAmount", discount);
            } catch (Exception e) {
                // Ignore invalid voucher on reload or add error message
            }
        }
        
        // --- ADD VOUCHERS LIST AGAIN ---
        List<Voucher> vouchers = voucherService.findVouchersForUser(user.getId(), cartTotal);
        model.addAttribute("availableVouchers", DtoMapper.toVoucherResponseList(vouchers));

        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("subtotal", cartTotal);
        model.addAttribute("totalAmount", cartTotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO));
        return "user/checkout";
    }

    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(30000);
    }
}   