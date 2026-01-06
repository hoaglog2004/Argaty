package com.argaty.controller.admin;

import com.argaty.dto.request.BrandRequest;
import com.argaty.entity.Brand;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.service.BrandService;
import com.argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý thương hiệu (Admin)
 */
@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    /**
     * Danh sách thương hiệu
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by("displayOrder").ascending());

        Page<Brand> brands;
        if (q != null && !q.trim().isEmpty()) {
            brands = brandService.search(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else {
            brands = brandService.findAll(pageRequest);
        }

        model.addAttribute("brands", brands);
        model.addAttribute("adminPage", "brands");

        return "admin/brands/list";
    }

    /**
     * Form tạo thương hiệu
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("brandRequest", new BrandRequest());
        model.addAttribute("adminPage", "brands");
        model.addAttribute("pageTitle", "Thêm thương hiệu");
        return "admin/brands/form";
    }

    /**
     * Xử lý tạo thương hiệu
     */
    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute BrandRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("adminPage", "brands");
            model.addAttribute("pageTitle", "Thêm thương hiệu");
            return "admin/brands/form";
        }

        try {
            brandService.create(
                    request.getName(),
                    request.getDescription(),
                    request.getLogo(),
                    request.getWebsite()
            );
            redirectAttributes.addFlashAttribute("success", "Thêm thương hiệu thành công");
            return "redirect:/admin/brands";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/brands/create";
        }
    }

    /**
     * Form sửa thương hiệu
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Brand brand = brandService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        model.addAttribute("brand", DtoMapper.toBrandResponse(brand));
        model.addAttribute("brandRequest", new BrandRequest());
        model.addAttribute("adminPage", "brands");
        model.addAttribute("pageTitle", "Sửa thương hiệu");
        model.addAttribute("isEdit", true);

        return "admin/brands/form";
    }

    /**
     * Xử lý cập nhật thương hiệu
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute BrandRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("adminPage", "brands");
            model.addAttribute("isEdit", true);
            return "admin/brands/form";
        }

        try {
            brandService.update(
                    id,
                    request.getName(),
                    request.getDescription(),
                    request.getLogo(),
                    request.getWebsite()
            );
            redirectAttributes.addFlashAttribute("success", "Cập nhật thương hiệu thành công");
            return "redirect:/admin/brands";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/brands/" + id + "/edit";
        }
    }

    /**
     * Toggle trạng thái active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        brandService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/brands";
    }

    /**
     * Xóa thương hiệu
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa thương hiệu");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/brands";
    }
}