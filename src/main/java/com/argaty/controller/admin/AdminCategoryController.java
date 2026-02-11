package com.argaty.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.request.CategoryRequest;
import com.argaty.entity.Category;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.service.CategoryService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
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
    public String create(@Valid @ModelAttribute CategoryRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, // Nhận file
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
            model.addAttribute("adminPage", "categories");
            model.addAttribute("pageTitle", "Thêm danh mục");
            return "admin/categories/form";
        }

        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = uploadImage(imageFile);
            }

            categoryService.create(
                    request.getName(),
                    request.getDescription(),
                    imageUrl,
                    request.getIcon(),
                    request.getParentId()
            );
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Đổ dữ liệu cũ vào request form
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(category.getName());
        categoryRequest.setSlug(category.getSlug());
        categoryRequest.setDescription(category.getDescription());
        categoryRequest.setImage(category.getImage()); // Set ảnh cũ
        categoryRequest.setIcon(category.getIcon());
        categoryRequest.setIsActive(category.getIsActive());
        categoryRequest.setIsFeatured(category.getIsFeatured());
        categoryRequest.setDisplayOrder(category.getDisplayOrder());
        if (category.getParent() != null) {
            categoryRequest.setParentId(category.getParent().getId());
        }

        model.addAttribute("category", DtoMapper.toCategoryResponse(category));
        model.addAttribute("categoryRequest", categoryRequest);
        model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
        model.addAttribute("adminPage", "categories");
        model.addAttribute("pageTitle", "Sửa danh mục");
        model.addAttribute("isEdit", true);
        model.addAttribute("id", id); // Quan trọng cho action form

        return "admin/categories/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute CategoryRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, // Nhận file
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("parentCategories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
            model.addAttribute("adminPage", "categories");
            model.addAttribute("isEdit", true);
            return "admin/categories/form";
        }

        try {
            String imageUrl = request.getImage();
            // Nếu có upload ảnh mới
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = uploadImage(imageFile);
            } else {
                // Giữ ảnh cũ
                Category oldCategory = categoryService.findById(id).orElseThrow();
                imageUrl = oldCategory.getImage();
            }

            categoryService.update(
                    id,
                    request.getName(),
                    request.getDescription(),
                    imageUrl,
                    request.getIcon(),
                    request.getParentId()
            );
            redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    // Các hàm toggle, delete giữ nguyên...
    // ...

    private String uploadImage(MultipartFile file) throws IOException {
        String uploadDir = "uploads/categories";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        return "/uploads/categories/" + fileName;
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 1. Thử xóa
            categoryService.deleteById(id);
            
            // 2. Nếu xóa được -> Báo thành công
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công!");
        } catch (DataIntegrityViolationException e) {
            // 3. Lỗi Ràng buộc dữ liệu (Danh mục đang có sản phẩm)
            redirectAttributes.addFlashAttribute("error", "Không thể xóa! Danh mục này đang chứa sản phẩm hoặc danh mục con.");
        } catch (Exception e) {
            // 4. Lỗi khác
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        
        // 5. Luôn quay về trang danh sách
        return "redirect:/admin/categories";
    }
}