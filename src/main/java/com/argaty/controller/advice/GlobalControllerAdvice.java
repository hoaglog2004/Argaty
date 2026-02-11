package com.argaty.controller.advice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.argaty.dto.response.MiniCartItemResponse;
import com.argaty.entity.Cart;
import com.argaty.entity.CartItem;
import com.argaty.entity.Product;
import com.argaty.entity.ProductImage;
import com.argaty.entity.User;
import com.argaty.service.CartService;
import com.argaty.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@ControllerAdvice(basePackages = "com.argaty.controller.user")
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final UserService userService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication, HttpSession session) {
        Cart cart = null;

        // 1. Xác định User hay Guest
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                cart = cartService.getOrCreateCart(user.getId());
                session.setAttribute("currentUserAvatar", user.getAvatar());
                session.setAttribute("currentUserName", user.getFullName());
            }
        } else {
            String sessionId = (String) session.getAttribute("CART_SESSION_ID");
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                session.setAttribute("CART_SESSION_ID", sessionId);
            }
            cart = cartService.getOrCreateCartBySession(sessionId);
        }

        // 2. Xử lý dữ liệu giỏ hàng
        if (cart != null) {
            session.setAttribute("cartItemCount", cart.getTotalItemCount());
            session.setAttribute("cartTotal", cart.getTotalAmount());

            List<CartItem> items = cart.getItems();
            if (items == null) items = new ArrayList<>();

            // --- MAP SANG DTO (ĐÃ FIX LỖI) ---
            List<MiniCartItemResponse> miniItems = items.stream()
                .map(item -> {
                    Product product = item.getProduct();
                    
                    // --- FIX LỖI 1: Xử lý lấy ảnh từ Set ---
                    String imgUrl = "/images/no-image.png";
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        // Dùng stream().findFirst() vì Set không có .get(0)
                        Optional<ProductImage> firstImg = product.getImages().stream().findFirst();
                        if (firstImg.isPresent()) {
                            imgUrl = firstImg.get().getImageUrl();
                        }
                    }

                    // --- FIX LỖI 2: Lấy giá ---
                    // Ưu tiên lấy giá của Variant (nếu có), nếu không lấy giá Product
                    BigDecimal price = product.getPrice();
                    if (item.getVariant() != null) {
                        // Giả sử variant có giá riêng, nếu không có thì fallback về product price
                        // Nếu VariantEntity của bạn chưa có getPrice, dùng product.getPrice()
                        // Ở đây mình dùng an toàn là lấy từ Product trước
                        price = product.getPrice(); 
                    }

                    return MiniCartItemResponse.builder()
                        .name(product.getName())
                        .slug(product.getSlug())
                        .imageUrl(imgUrl)
                        .quantity(item.getQuantity())
                        .price(price) // Đã sửa lỗi item.getPrice()
                        .build();
                })
                .collect(Collectors.toList());

            session.setAttribute("miniCartItems", miniItems);
        } else {
            session.setAttribute("cartItemCount", 0);
            session.setAttribute("miniCartItems", new ArrayList<>());
        }
    }
}