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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.request.ProductRequest;
import com.argaty.entity.Product;
import com.argaty.entity.ProductVariant;
import com.argaty.exception.BadRequestException;
import com.argaty.service.BrandService;
import com.argaty.service.CategoryService;
import com.argaty.service.ProductService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
            // Admin: tìm tất cả sản phẩm (kể cả inactive)
            products = productService.searchAll(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else if (categoryId != null) {
            // Admin: lấy tất cả sản phẩm theo category (kể cả inactive)
            products = productService.findAllByCategory(categoryId, pageRequest);
            model.addAttribute("selectedCategoryId", categoryId);
        } else if (brandId != null) {
            // Admin: lấy tất cả sản phẩm theo brand (kể cả inactive)
            products = productService.findAllByBrand(brandId, pageRequest);
            model.addAttribute("selectedBrandId", brandId);
        } else {
            // Admin: lấy tất cả sản phẩm
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
            // Validate business rules
            if (request.getSalePrice() != null && request.getPrice() != null 
                && request.getSalePrice().compareTo(request.getPrice()) >= 0) {
                bindingResult.rejectValue("salePrice", "error.salePrice", 
                    "Giá sale phải nhỏ hơn giá gốc");
                model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
                model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
                model.addAttribute("adminPage", "products");
                model.addAttribute("pageTitle", "Thêm sản phẩm");
                return "admin/products/form";
            }

            Product product = productService.create(
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
                    request.getIsBestSeller(),
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
                    ProductVariant savedVariant = productService.addVariant(
                            product.getId(),
                            v.getName(),
                            v.getColor(),
                            v.getColorCode(),
                            v.getSize(),
                            v.getAdditionalPrice(),
                            v.getQuantity()
                    );

                    if (v.getImageUrls() != null) {
                        for (int j = 0; j < v.getImageUrls().size(); j++) {
                            productService.addVariantImage(savedVariant.getId(), v.getImageUrls().get(j), j == 0);
                        }
                    }
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
        model.addAttribute("productRequest", convertToProductRequest(product));
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
            // Validate business rules
            if (request.getSalePrice() != null && request.getPrice() != null 
                && request.getSalePrice().compareTo(request.getPrice()) >= 0) {
                bindingResult.rejectValue("salePrice", "error.salePrice", 
                    "Giá sale phải nhỏ hơn giá gốc");
                Product product = productService.findByIdWithDetails(id).orElse(null);
                model.addAttribute("product", product != null ? DtoMapper.toProductDetailResponse(product) : null);
                model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
                model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
                model.addAttribute("adminPage", "products");
                model.addAttribute("isEdit", true);
                return "admin/products/form";
            }

            productService.update(
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
                    request.getIsBestSeller(),
                    request.getSpecifications(),
                    request.getMetaTitle(),
                    request.getMetaDescription(),
                    request.getSaleStartDate(),
                    request.getSaleEndDate()
            );

            // Handle new images
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                for (int i = 0; i < request.getImageUrls().size(); i++) {
                    productService.addImage(id, request.getImageUrls().get(i), i == 0);
                }
            }

            // Handle new variants
            if (request.getVariants() != null && !request.getVariants().isEmpty()) {
                for (ProductRequest.ProductVariantRequest v : request.getVariants()) {
                    ProductVariant savedVariant = productService.addVariant(
                            id,
                            v.getName(),
                            v.getColor(),
                            v.getColorCode(),
                            v.getSize(),
                            v.getAdditionalPrice(),
                            v.getQuantity()
                    );

                    if (v.getImageUrls() != null && !v.getImageUrls().isEmpty()) {
                        for (int j = 0; j < v.getImageUrls().size(); j++) {
                            productService.addVariantImage(savedVariant.getId(), v.getImageUrls().get(j), j == 0);
                        }
                    }
                }
            }

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

    /**
     * Helper method to convert Product entity to ProductRequest DTO
     * Used for populating the edit form with existing product data
     */
    private ProductRequest convertToProductRequest(Product product) {
        if (product == null) {
            return new ProductRequest();
        }

        return ProductRequest.builder()
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .discountPercent(product.getDiscountPercent())
                .quantity(product.getQuantity())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .isFeatured(product.getIsFeatured())
                .isNew(product.getIsNew())
                .isBestSeller(product.getIsBestSeller())
                .isActive(product.getIsActive())
                .saleStartDate(product.getSaleStartDate())
                .saleEndDate(product.getSaleEndDate())
                .specifications(product.getSpecifications())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .build();
    }
}