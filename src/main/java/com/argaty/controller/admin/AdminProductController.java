package com.argaty.controller.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.argaty.dto.request.ProductVariantDTO;
import com.argaty.entity.Product;
import com.argaty.service.BrandService;
import com.argaty.service.CategoryService;
import com.argaty.service.ProductService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý sản phẩm (Admin) - Đã tối ưu hóa
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    // --- LIST (Giữ nguyên) ---
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortField, // Mặc định sắp xếp theo ngày tạo
            @RequestParam(defaultValue = "desc") String sortDir,        // Mặc định giảm dần
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        // 1. Xử lý Sắp xếp (Sort)
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // 2. Gọi hàm Filter Master (thay vì if-else lằng nhằng)
        Page<Product> products = productService.filterProducts(q, categoryId, brandId, minPrice, maxPrice, pageRequest);

        // 3. Đẩy dữ liệu ra View
        model.addAttribute("products", DtoMapper.toProductPageResponse(products));
        model.addAttribute("categories", DtoMapper.toCategoryResponseList(categoryService.findAllActive()));
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
        
        // 4. Giữ lại các giá trị filter trên form để người dùng biết mình đang lọc gì
        model.addAttribute("searchKeyword", q);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        
        model.addAttribute("adminPage", "products");

        return "admin/products/list";
    }
    // --- CREATE FORM (Giữ nguyên) ---
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        loadCommonAttributes(model);
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        return "admin/products/form";
    }

    // --- CREATE ACTION ---
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute ProductRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            loadCommonAttributes(model);
            model.addAttribute("pageTitle", "Thêm sản phẩm");
            return "admin/products/form";
        }

        try {
            // Logic tạo mới vẫn có thể giữ nguyên hoặc chuyển vào service.create(request) 
            // Nhưng để an toàn với code cũ, ta giữ nguyên cách gọi hàm create cũ, 
            // chỉ lưu ý phần variants phải dùng DTO mới.
            
            // ... (Phần validate giá sale giữ nguyên) ...

            // Gọi service tạo mới (Lưu ý: Nếu bạn đã sửa Service create nhận Request thì đổi ở đây)
            // Giả sử service.create vẫn nhận từng tham số như cũ:
            Product product = productService.create(
                    request.getName(),request.getSku(), request.getShortDescription(), request.getDescription(),
                    request.getPrice(), request.getSalePrice(), request.getDiscountPercent(),
                    request.getQuantity(), request.getCategoryId(), request.getBrandId(),
                    request.getIsFeatured(), request.getIsNew(), request.getIsBestSeller(),
                    request.getSpecifications(), request.getMetaTitle(), request.getMetaDescription(),
                    request.getSaleStartDate(), request.getSaleEndDate()
            );

            // Xử lý ảnh và variants (Code cũ của bạn ok, chỉ cần sửa kiểu dữ liệu vòng lặp)
            if (request.getImageUrls() != null) {
                for (int i = 0; i < request.getImageUrls().size(); i++) {
                    productService.addImage(product.getId(), request.getImageUrls().get(i), i == 0);
                }
            }

            if (request.getVariants() != null) {
                for (ProductVariantDTO v : request.getVariants()) { // <-- Dùng DTO mới
                    var savedVariant = productService.addVariant(
                            product.getId(), v.getName(), v.getColor(), v.getColorCode(),
                            v.getSize(), v.getAdditionalPrice(), v.getQuantity()
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

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    // --- EDIT FORM (Giữ nguyên) ---
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findByIdWithDetails(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Product", "id", id));

        model.addAttribute("product", DtoMapper.toProductDetailResponse(product));
        model.addAttribute("productRequest", convertToProductRequest(product));
        loadCommonAttributes(model);
        model.addAttribute("pageTitle", "Sửa sản phẩm");
        model.addAttribute("isEdit", true);
        return "admin/products/form";
    }

    // --- UPDATE ACTION (ĐÃ SỬA LẠI HOÀN TOÀN) ---
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ProductRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            // Reload lại data để hiển thị form lỗi
            Product product = productService.findByIdWithDetails(id).orElse(null);
            model.addAttribute("product", product != null ? DtoMapper.toProductDetailResponse(product) : null);
            loadCommonAttributes(model);
            model.addAttribute("pageTitle", "Sửa sản phẩm");
            model.addAttribute("isEdit", true);
            return "admin/products/form";
        }

        try {
            // [QUAN TRỌNG] Gọi hàm update mới trong Service, truyền toàn bộ Request vào
            // Service sẽ lo việc xóa ảnh cũ, thêm ảnh mới, đồng bộ variants...
            productService.update(id, request); 

            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
            return "redirect:/admin/products";

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }

    // --- CÁC HÀM TOGGLE/DELETE GIỮ NGUYÊN ---
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/products";
    }
    // ... toggleFeatured, toggleNew, delete ... (Giữ nguyên)

    // --- HELPER METHODS ---
    private void loadCommonAttributes(Model model) {
        model.addAttribute("categories", DtoMapper.toCategoryWithChildrenResponseList(categoryService.findRootCategories()));
        model.addAttribute("brands", DtoMapper.toBrandResponseList(brandService.findAllActive()));
        model.addAttribute("adminPage", "products");
    }

    private ProductRequest convertToProductRequest(Product product) {
        if (product == null) return new ProductRequest();

        // Convert Variants Entity -> DTO
        List<ProductVariantDTO> variantDTOs = new ArrayList<>();
        if (product.getVariants() != null) {
            variantDTOs = product.getVariants().stream().map(v -> ProductVariantDTO.builder()
                    .id(v.getId())
                    .name(v.getName())
                    .sku(v.getSku())
                    .color(v.getColor())
                    .colorCode(v.getColorCode())
                    .size(v.getSize())
                    .additionalPrice(v.getAdditionalPrice())
                    .quantity(v.getQuantity())
                    // Map variant images nếu cần
                    .build()).collect(Collectors.toList());
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
                .variants(variantDTOs) // Set list variants
                .build();
    }
}