package com.argaty.controller.admin;

import com.argaty.dto.request.ProductRequest;
import com.argaty.entity.Product;
import com.argaty.exception.BadRequestException;
import com.argaty.service.BrandService;
import com.argaty.service.CategoryService;
import com.argaty.service.ProductService;
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
 * Controller quản lý sản phẩm (Admin)
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    /**
     * Danh sách sản phẩm
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> products;
        if (q != null && !q.trim().isEmpty()) {
            products = productService.search(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else if (categoryId != null) {
            products = productService.findByCategory(categoryId, pageRequest);
            model.addAttribute("selectedCategoryId", categoryId);
        } else if (brandId != null) {
            products = productService.findByBrand(brandId, pageRequest);
            model.addAttribute("selectedBrandId", brandId);
        } else {
            products = productService.findAll(pageRequest);
        }

        model.addAttribute("products", DtoMapper.toProductPageResponse(products));
        model.addAttribute("categories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
        model.addAttribute("adminPage", "products");

        return "admin/products/list";
    }

    /**
     * Form tạo sản phẩm mới
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
        model.addAttribute("adminPage", "products");
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        
        return "admin/products/form";
    }

    /**
     * Xử lý tạo sản phẩm
     */
    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute ProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
            model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
            model.addAttribute("adminPage", "products");
            model.addAttribute("pageTitle", "Thêm sản phẩm");
            return "admin/products/form";
        }

        try {
            Product product = ((com.argaty.service.impl.ProductServiceImpl) productService).createWithExtras(
                    request.getName(),
                    request.getShortDescription(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getSalePrice(),
                    request.getDiscountPercent(),
                    request.getQuantity(),
                    request.getCategoryId(),
                    request.getBrandId(),
                    request.getIsFeatured(),
                    request.getIsNew(),
                    request.getSpecifications(),
                    request.getMetaTitle(),
                    request.getMetaDescription(),
                    request.getSaleStartDate(),
                    request.getSaleEndDate()
            );

            // Thêm ảnh
            if (request.getImageUrls() != null) {
                for (int i = 0; i < request.getImageUrls().size(); i++) {
                    productService.addImage(product.getId(), request.getImageUrls().get(i), i == 0);
                }
            }

            // Thêm variants
            if (request.getVariants() != null) {
                for (ProductRequest.ProductVariantRequest v : request.getVariants()) {
                    productService.addVariant(
                            product.getId(),
                            v.getName(),
                            v.getColor(),
                            v.getColorCode(),
                            v.getSize(),
                            v.getAdditionalPrice(),
                            v.getQuantity()
                    );
                }
            }

            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công");
            return "redirect:/admin/products";

        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    /**
     * Form chỉnh sửa sản phẩm
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findByIdWithDetails(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Product", "id", id));

        model.addAttribute("product", DtoMapper.toProductDetailResponse(product));
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
        model.addAttribute("adminPage", "products");
        model.addAttribute("pageTitle", "Sửa sản phẩm");
        model.addAttribute("isEdit", true);

        return "admin/products/form";
    }

    /**
     * Xử lý cập nhật sản phẩm
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Product product = productService.findByIdWithDetails(id).orElse(null);
            model.addAttribute("product", product != null ? DtoMapper.toProductDetailResponse(product) : null);
            model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
            model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
            model.addAttribute("adminPage", "products");
            model.addAttribute("isEdit", true);
            return "admin/products/form";
        }

        try {
            ((com.argaty.service.impl.ProductServiceImpl) productService).updateWithExtras(
                    id,
                    request.getName(),
                    request.getShortDescription(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getSalePrice(),
                    request.getDiscountPercent(),
                    request.getQuantity(),
                    request.getCategoryId(),
                    request.getBrandId(),
                    request.getIsFeatured(),
                    request.getIsNew(),
                    request.getSpecifications(),
                    request.getMetaTitle(),
                    request.getMetaDescription(),
                    request.getSaleStartDate(),
                    request.getSaleEndDate()
            );

            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
            return "redirect:/admin/products";

        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }

    /**
     * Toggle trạng thái active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái sản phẩm");
        return "redirect:/admin/products";
    }

    /**
     * Toggle featured
     */
    @PostMapping("/{id}/toggle-featured")
    public String toggleFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.toggleFeatured(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái nổi bật");
        return "redirect:/admin/products";
    }

    /**
     * Toggle is_new status
     */
    @PostMapping("/{id}/toggle-new")
    public String toggleNew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.toggleNew(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái sản phẩm mới");
        return "redirect:/admin/products";
    }

    /**
     * Xóa sản phẩm
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}