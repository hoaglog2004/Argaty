package com.argaty.controller.admin;

import com.argaty.dto.request.CategoryRequest;
import com.argaty.entity.Category;
import com.argaty.exception.BadRequestException;
import com.argaty.service.CategoryService;
import com.argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý danh mục (Admin)
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by("displayOrder").ascending());

        Page<Category> categories;
        if (q != null && !q.trim().isEmpty()) {
            categories = categoryService.search(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else {
            categories = categoryService.findAll(pageRequest);
        }

        model.addAttribute("categories", DtoMapper.toCategoryPageResponse(categories));
        model.addAttribute("rootCategories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
        model.addAttribute("adminPage", "categories");

        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("categoryRequest", new CategoryRequest());
        model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
        model.addAttribute("adminPage", "categories");
        model.addAttribute("pageTitle", "Thêm danh mục");
        return "admin/categories/form";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute CategoryRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
            model.addAttribute("adminPage", "categories");
            return "admin/categories/form";
        }

        try {
            categoryService.create(
                    request.getName(),
                    request.getDescription(),
                    request.getImage(),
                    request.getIcon(),
                    request.getParentId()
            );
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công");
            return "redirect:/admin/categories";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Category", "id", id));

        model.addAttribute("category", DtoMapper.toCategoryResponse(category));
        model.addAttribute("categoryRequest", new CategoryRequest());
        model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
        model.addAttribute("adminPage", "categories");
        model.addAttribute("pageTitle", "Sửa danh mục");
        model.addAttribute("isEdit", true);

        return "admin/categories/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute CategoryRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
            model.addAttribute("adminPage", "categories");
            model.addAttribute("isEdit", true);
            return "admin/categories/form";
        }

        try {
            categoryService.update(
                    id,
                    request.getName(),
                    request.getDescription(),
                    request.getImage(),
                    request.getIcon(),
                    request.getParentId()
            );
            redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công");
            return "redirect:/admin/categories";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/toggle-featured")
    public String toggleFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.toggleFeatured(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái nổi bật");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}