package com.argaty.controller.admin;

import com.argaty.dto.request.VoucherRequest;
import com.argaty.entity.Voucher;
import com.argaty.exception.BadRequestException;
import com.argaty.service.VoucherService;
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
 * Controller quản lý voucher (Admin)
 */
@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    /**
     * Danh sách voucher
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Voucher> vouchers;
        if (q != null && !q.trim().isEmpty()) {
            vouchers = voucherService.search(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else {
            vouchers = voucherService.findAll(pageRequest);
        }

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("adminPage", "vouchers");

        return "admin/vouchers/list";
    }

    /**
     * Form tạo voucher
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("voucherRequest", new VoucherRequest());
        model.addAttribute("adminPage", "vouchers");
        model.addAttribute("pageTitle", "Thêm voucher");
        return "admin/vouchers/form";
    }

    /**
     * Xử lý tạo voucher
     */
    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute VoucherRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("adminPage", "vouchers");
            model.addAttribute("pageTitle", "Thêm voucher");
            return "admin/vouchers/form";
        }

        try {
            voucherService.create(
                    request.getCode(),
                    request.getName(),
                    request.getDescription(),
                    request.getDiscountType(),
                    request.getDiscountValue(),
                    request.getMaxDiscount(),
                    request.getMinOrderAmount(),
                    request.getUsageLimit(),
                    request.getUsageLimitPerUser(),
                    request.getStartDate(),
                    request.getEndDate()
            );
            redirectAttributes.addFlashAttribute("success", "Thêm voucher thành công");
            return "redirect:/admin/vouchers";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/vouchers/create";
        }
    }

    /**
     * Form sửa voucher
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Voucher", "id", id));

        model.addAttribute("voucher", DtoMapper.toVoucherResponse(voucher));
        model.addAttribute("voucherRequest", new VoucherRequest());
        model.addAttribute("adminPage", "vouchers");
        model.addAttribute("pageTitle", "Sửa voucher");
        model.addAttribute("isEdit", true);

        return "admin/vouchers/form";
    }

    /**
     * Xử lý cập nhật voucher
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute VoucherRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("adminPage", "vouchers");
            model.addAttribute("isEdit", true);
            return "admin/vouchers/form";
        }

        try {
            voucherService.update(
                    id,
                    request.getName(),
                    request.getDescription(),
                    request.getDiscountType(),
                    request.getDiscountValue(),
                    request.getMaxDiscount(),
                    request.getMinOrderAmount(),
                    request.getUsageLimit(),
                    request.getUsageLimitPerUser(),
                    request.getStartDate(),
                    request.getEndDate()
            );
            redirectAttributes.addFlashAttribute("success", "Cập nhật voucher thành công");
            return "redirect:/admin/vouchers";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/vouchers/" + id + "/edit";
        }
    }

    /**
     * Toggle trạng thái active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        voucherService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/vouchers";
    }

    /**
     * Xóa voucher
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa voucher");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa voucher");
        }
        return "redirect:/admin/vouchers";
    }
}