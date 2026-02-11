package com.argaty.controller.admin;

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

import com.argaty.dto.request.BannerRequest;
import com.argaty.entity.Banner;
import com.argaty.exception.BadRequestException;
import com.argaty.service.BannerService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by("displayOrder").ascending());

        Page<Banner> banners = bannerService.findAll(pageRequest);

        model.addAttribute("banners", banners);
        model.addAttribute("positions", new String[]{
                Banner.POSITION_HOME_SLIDER,
                Banner.POSITION_HOME_BANNER,
                Banner.POSITION_PRODUCT_BANNER,
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
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1. Validate dữ liệu form
        if (bindingResult.hasErrors()) {
            // Setup lại model để hiển thị lại form
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

        try {
            String imageUrl = request.getImageUrl();
            // 2. Xử lý ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = uploadImage(imageFile);
            } else if (imageUrl == null || imageUrl.isEmpty()) {
                // Tạo mới bắt buộc phải có ảnh
                throw new BadRequestException("Vui lòng chọn hình ảnh cho banner mới!");
            }

            // 3. Gọi service tạo mới
            bannerService.create(
                    request.getTitle(),
                    request.getSubtitle(),
                    imageUrl,
                    request.getLink(),
                    request.getPosition(),
                    request.getDisplayOrder(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            redirectAttributes.addFlashAttribute("success", "Thêm banner thành công");
            return "redirect:/admin/banners";

        } catch (Exception e) { // Bắt Exception chung để tránh lỗi 500
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
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
                Banner.POSITION_HOME_BANNER,
                Banner.POSITION_PRODUCT_BANNER,
                Banner.POSITION_POPUP
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
                    Banner.POSITION_HOME_SLIDER,
                    Banner.POSITION_HOME_BANNER,
                    Banner.POSITION_PRODUCT_BANNER,
                    Banner.POSITION_POPUP
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
                    request.getImageUrl(),
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
            redirectAttributes.addFlashAttribute("success", "Đã xóa banner");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa banner");
        }
        return "redirect:/admin/banners";
    }

    /**
     * Upload ảnh banner và trả về URL
     */
    private String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh banner");
        }
        try {
            // Tạo thư mục uploads/banners nếu chưa có
            String uploadDir = "uploads/banners/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            // Đặt tên file duy nhất
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String filename = System.currentTimeMillis() + "-" + java.util.UUID.randomUUID() + ext;
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            // Lưu file
            file.transferTo(filePath.toAbsolutePath().toFile());
            // Trả về URL tương đối để dùng trong src
            return "/" + uploadDir.replace("\\", "/") + filename;
        } catch (Exception e) {
            throw new BadRequestException("Không thể upload ảnh: " + e.getMessage());
        }
    }
}