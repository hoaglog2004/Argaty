package com.argaty.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

import com.argaty.dto.request.BrandRequest;
import com.argaty.entity.Brand;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.service.BrandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile, // <-- QUAN TRỌNG: Nhận file từ form
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("adminPage", "brands");
            model.addAttribute("pageTitle", "Thêm thương hiệu");
            return "admin/brands/form";
        }

        try {
            // Xử lý upload ảnh logo
            String logoUrl = null;
            if (logoFile != null && !logoFile.isEmpty()) {
                logoUrl = uploadImage(logoFile);
            }

            // Gọi service với logoUrl vừa lấy được
            brandService.create(
                    request.getName(),
                    request.getSlug(), // Form của bạn có field slug
                    logoUrl,           // Truyền URL ảnh vào đây
                    request.getDescription(),
                    request.getIsActive() // Form của bạn có field isActive
            );
            
            redirectAttributes.addFlashAttribute("success", "Thêm thương hiệu thành công");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/brands/create";
        }
    }

    /**
     * Form sửa thương hiệu
     */
   @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        // 1. Tìm Brand cũ
        Brand brand = brandService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // 2. Tạo Request và ĐỔ DỮ LIỆU CŨ VÀO (QUAN TRỌNG)
        BrandRequest brandRequest = new BrandRequest();
        brandRequest.setName(brand.getName());
        brandRequest.setSlug(brand.getSlug());
        brandRequest.setDescription(brand.getDescription());
        brandRequest.setLogo(brand.getLogo()); // Dòng này giúp hiển thị ảnh cũ trong form
        brandRequest.setIsActive(brand.getIsActive());

        // 3. Gửi sang View
        model.addAttribute("brandRequest", brandRequest);
        model.addAttribute("id", id); // Gửi ID để dùng trong th:action
        
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
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile, // <-- QUAN TRỌNG
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("adminPage", "brands");
            model.addAttribute("isEdit", true);
            return "admin/brands/form";
        }

        try {
            String logoUrl = request.getLogo();

            // Nếu người dùng chọn ảnh mới -> Upload và lấy URL mới
            if (logoFile != null && !logoFile.isEmpty()) {
                logoUrl = uploadImage(logoFile);
            } else {
                // Nếu không chọn ảnh mới -> Lấy lại ảnh cũ từ Database
                Brand existingBrand = brandService.findById(id).orElseThrow();
                logoUrl = existingBrand.getLogo();
            }

            brandService.update(
                    id,
                    request.getName(),
                    request.getSlug(),
                    logoUrl, // URL mới hoặc cũ
                    request.getDescription(),
                    request.getIsActive()
            );
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thương hiệu thành công");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
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

    /**
     * Hàm xử lý upload ảnh (Helper method)
     */
    private String uploadImage(MultipartFile file) throws IOException {
        // Tạo thư mục uploads/brands nếu chưa có
        String uploadDir = "uploads/brands";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file độc nhất
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Trả về đường dẫn để lưu vào DB
        return "/uploads/brands/" + fileName;
    }
}