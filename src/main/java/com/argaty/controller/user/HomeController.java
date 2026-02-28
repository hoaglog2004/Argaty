package com.argaty.controller.user;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.entity.Banner;
import com.argaty.entity.Category;
import com.argaty.entity.Product;
import com.argaty.service.BannerService;
import com.argaty.service.BrandService;
import com.argaty.service.CategoryService;
import com.argaty.service.EmailService;
import com.argaty.service.ProductService;
import com.argaty.util.DtoMapper;

import lombok.RequiredArgsConstructor;

/**
 * Controller cho trang chủ
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final BannerService bannerService;
    private final EmailService emailService;

    /**
     * Trang chủ
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Banners
        List<Banner> sliderBanners = bannerService.findActiveByPosition(Banner.POSITION_HOME_SLIDER);
        model.addAttribute("sliderBanners", DtoMapper.toBannerResponseList(sliderBanners));

        // Featured categories
        List<Category> featuredCategories = categoryService.findFeaturedCategories();
        model.addAttribute("featuredCategories", DtoMapper.toCategoryResponseList(featuredCategories));

        // Featured products
        List<Product> featuredProducts = productService.findFeaturedProducts(8);
        model.addAttribute("featuredProducts", DtoMapper.toProductResponseList(featuredProducts));

        // New products
        List<Product> newProducts = productService.findNewProducts(8);
        model.addAttribute("newProducts", DtoMapper.toProductResponseList(newProducts));

        // Best seller products
        List<Product> bestSellerProducts = productService.findBestSellerProducts(8);
        model.addAttribute("bestSellerProducts", DtoMapper.toProductResponseList(bestSellerProducts));

        // Brands
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));

        model.addAttribute("currentPage", "home");
        return "user/home";
    }

    @PostMapping("/newsletter/subscribe")
    public String subscribeNewsletter(@RequestParam(required = false) String email,
                                      RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ. Vui lòng nhập lại.");
            return "redirect:/#newsletter";
        }

        String normalizedEmail = email.trim();

        try {
            emailService.sendNewsletterSubscriptionEmail(normalizedEmail);
            redirectAttributes.addFlashAttribute("success", "Đăng ký nhận tin thành công! Vui lòng kiểm tra email.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể đăng ký nhận tin lúc này. Vui lòng thử lại sau.");
        }

        return "redirect:/#newsletter";
    }

    /**
     * Trang giới thiệu
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPage", "about");
        return "user/about";
    }

    /**
     * Trang liên hệ
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("currentPage", "contact");
        return "user/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam(required = false) String name,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String subject,
                                @RequestParam(required = false) String message,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String normalizedName = normalize(name);
        String normalizedEmail = normalize(email);
        String normalizedPhone = normalize(phone);
        String normalizedSubject = normalize(subject);
        String normalizedMessage = normalize(message);

        if (!StringUtils.hasText(normalizedName)
                || !StringUtils.hasText(normalizedEmail)
                || !StringUtils.hasText(normalizedSubject)
                || !StringUtils.hasText(normalizedMessage)) {
            return renderContactWithError(model, "Vui lòng nhập đầy đủ các trường bắt buộc.",
                    normalizedName, normalizedEmail, normalizedPhone, normalizedSubject, normalizedMessage);
        }

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return renderContactWithError(model, "Email không hợp lệ. Vui lòng kiểm tra lại.",
                    normalizedName, normalizedEmail, normalizedPhone, normalizedSubject, normalizedMessage);
        }

        try {
            emailService.sendContactMessageEmail(
                    normalizedName,
                    normalizedEmail,
                    normalizedPhone,
                    mapContactSubject(normalizedSubject),
                    normalizedMessage
            );
            redirectAttributes.addFlashAttribute("success", "Tin nhắn của bạn đã được gửi thành công. Chúng tôi sẽ phản hồi sớm nhất!");
            return "redirect:/contact";
        } catch (Exception e) {
            return renderContactWithError(model, "Không thể gửi tin nhắn lúc này. Vui lòng thử lại sau.",
                    normalizedName, normalizedEmail, normalizedPhone, normalizedSubject, normalizedMessage);
        }
    }

    private String renderContactWithError(Model model,
                                          String error,
                                          String name,
                                          String email,
                                          String phone,
                                          String subject,
                                          String message) {
        model.addAttribute("error", error);
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("subject", subject);
        model.addAttribute("message", message);
        model.addAttribute("currentPage", "contact");
        return "user/contact";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String mapContactSubject(String subject) {
        return switch (subject) {
            case "order" -> "Hỏi về đơn hàng";
            case "product" -> "Hỏi về sản phẩm";
            case "warranty" -> "Bảo hành";
            case "complaint" -> "Khiếu nại";
            case "other" -> "Khác";
            default -> subject;
        };
    }

    /**
     * Trang FAQ
     */
    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("currentPage", "faq");
        return "user/faq";
    }

    /**
     * Trang chính sách
     */
    @GetMapping("/policy/{type}")
    public String policy(@org.springframework.web.bind.annotation.PathVariable String type, Model model) {
        model.addAttribute("policyType", type);
        return "user/policy";
    }
}