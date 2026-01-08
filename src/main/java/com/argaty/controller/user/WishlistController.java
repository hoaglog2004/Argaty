package com.argaty.controller.user;

import com.argaty.entity.User;
import com.argaty.entity.Wishlist;
import com.argaty.service.UserService;
import com.argaty.service.WishlistService;
import com.argaty.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

/**
 * Controller cho wishlist (trang riêng)
 */
@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    @GetMapping
    public String wishlist(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login?redirect=/wishlist";
        }

        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

        List<Wishlist> wishlists = wishlistService.findByUserIdWithProduct(user.getId());
        model.addAttribute("wishlists", DtoMapper.toWishlistResponseList(wishlists));
        model.addAttribute("currentPage", "wishlist");

        return "user/wishlist";
    }
}