package com.argaty.controller.admin;

import com.argaty.dto.request.BannerRequest;
import com.argaty.entity.Banner;
import com.argaty.exception.BadRequestException;
import com.argaty.service.BannerService;
import com.argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain. Page;
import org.springframework. data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation. BindingResult;
import org. springframework.web.bind.annotation.*;
import org.springframework.web. servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý banner (Admin)
 */
@Controller
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    /**
     * Danh sách banner
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest. of(page, 20, Sort.by("displayOrder").ascending());

        Page<Banner> banners = bannerService.findAll(pageRequest);

        model.addAttribute("banners", banners);
        model.addAttribute("positions", new String[]{
                Banner. POSITION_HOME_SLIDER,
                Banner.POSITION_HOME_BANNER,
                Banner. POSITION_PRODUCT_BANNER,
                Banner.POSITION_POPUP
        });
        model.addAttribute("adminPage", "banners");

        return "admin/banners/list";
    }

    /**
     * Form tạo banner
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("bannerRequest", new BannerRequest());
        model.addAttribute("positions", new String[]{
                Banner.POSITION_HOME_SLIDER,
                Banner.POSITION_HOME_BANNER,
                Banner.POSITION_PRODUCT_BANNER,
                Banner.POSITION_POPUP
        });
        model.addAttribute("adminPage", "banners");
        model.addAttribute("pageTitle", "Thêm banner");
        return "admin/banners/form";
    }

    /**
     * Xử lý tạo banner
     */
    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute BannerRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult. hasErrors()) {
            model. addAttribute("positions", new String[]{
                    Banner.POSITION_HOME_SLIDER,
                    Banner. POSITION_HOME_BANNER,
                    Banner.POSITION_PRODUCT_BANNER,
                    Banner.POSITION_POPUP
            });
            model.addAttribute("adminPage", "banners");
            model.addAttribute("pageTitle", "Thêm banner");
            return "admin/banners/form";
        }

        try {
            bannerService.create(
                    request. getTitle(),
                    request.getSubtitle(),
                    request. getImageUrl(),
                    request.getLink(),
                    request.getPosition(),
                    request.getDisplayOrder(),
                    request.getStartDate(),
                    request.getEndDate()
            );
            redirectAttributes. addFlashAttribute("success", "Thêm banner thành công");
            return "redirect:/admin/banners";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/banners/create";
        }
    }

    /**
     * Form sửa banner
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Banner banner = bannerService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Banner", "id", id));

        model.addAttribute("banner", DtoMapper.toBannerResponse(banner));
        model.addAttribute("bannerRequest", new BannerRequest());
        model.addAttribute("positions", new String[]{
                Banner.POSITION_HOME_SLIDER,
                Banner. POSITION_HOME_BANNER,
                Banner.POSITION_PRODUCT_BANNER,
                Banner. POSITION_POPUP
        });
        model.addAttribute("adminPage", "banners");
        model.addAttribute("pageTitle", "Sửa banner");
        model.addAttribute("isEdit", true);

        return "admin/banners/form";
    }

    /**
     * Xử lý cập nhật banner
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute BannerRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", new String[]{
                    Banner. POSITION_HOME_SLIDER,
                    Banner.POSITION_HOME_BANNER,
                    Banner.POSITION_PRODUCT_BANNER,
                    Banner. POSITION_POPUP
            });
            model.addAttribute("adminPage", "banners");
            model.addAttribute("isEdit", true);
            return "admin/banners/form";
        }

        try {
            bannerService.update(
                    id,
                    request.getTitle(),
                    request.getSubtitle(),
                    request. getImageUrl(),
                    request.getLink(),
                    request.getPosition(),
                    request.getDisplayOrder(),
                    request.getStartDate(),
                    request.getEndDate()
            );
            redirectAttributes.addFlashAttribute("success", "Cập nhật banner thành công");
            return "redirect:/admin/banners";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/banners/" + id + "/edit";
        }
    }

    /**
     * Toggle trạng thái active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bannerService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/banners";
    }

    /**
     * Xóa banner
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.deleteById(id);
            redirectAttributes. addFlashAttribute("success", "Đã xóa banner");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa banner");
        }
        return "redirect:/admin/banners";
    }
}