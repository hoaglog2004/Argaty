package com.argaty.controller.user;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.argaty.entity.Banner;
import com.argaty.entity.Category;
import com.argaty.entity.Product;
import com.argaty.service.BannerService;
import com.argaty.service.BrandService;
import com.argaty.service.CategoryService;
import com.argaty.service.ProductService;
import com.argaty.util.DtoMapper;

import lombok.RequiredArgsConstructor;

/**
 * Controller cho trang chủ
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final BannerService bannerService;

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