package com.argaty.controller.admin;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.request.UpdateOrderStatusRequest;
import com.argaty.entity.Order;
import com.argaty.entity.User;
import com.argaty.enums.OrderStatus;
import com.argaty.service.OrderService;
import com.argaty.service.UserService;
import com.argaty.util.DtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final UserService userService;

    // --- DANH SÁCH ĐƠN HÀNG ---
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders;

        if (q != null && !q.trim().isEmpty()) {
            orders = orderService.searchOrders(q.trim(), pageRequest);
            model.addAttribute("searchKeyword", q);
        } else if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.findByStatus(orderStatus, pageRequest);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                orders = orderService.findAll(pageRequest);
            }
        } else {
            orders = orderService.findAll(pageRequest);
        }

        model.addAttribute("orders", DtoMapper.toOrderPageResponse(orders));
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("adminPage", "orders");

        return "admin/orders/list";
    }

    // --- CHI TIẾT ĐƠN HÀNG ---
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findByIdWithDetails(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "id", id));

        // SỬA: Dùng DtoMapper để thống nhất và tránh lỗi nếu DTO chưa có hàm static
        model.addAttribute("order", DtoMapper.toOrderDetailResponse(order));
        
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("adminPage", "orders");

        return "admin/orders/detail";
    }

    // --- CẬP NHẬT TRẠNG THÁI ---
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateOrderStatusRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(principal.getName()).orElseThrow();
            orderService.updateStatus(id, request.getStatus(), admin, request.getNote());
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // --- MARK PAID (ĐÁNH DẤU ĐÃ THANH TOÁN) ---
    // Controller cũ bạn thiếu cái này nhưng HTML lại gọi, nên tôi thêm vào để tránh lỗi 404/405
    @PostMapping("/{id}/mark-paid")
    public String markPaid(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Giả sử bạn có hàm này trong Service, nếu chưa thì cần thêm vào OrderService
            // orderService.markAsPaid(id); 
            // Tạm thời comment để không lỗi biên dịch, bạn cần implement logic này
             redirectAttributes.addFlashAttribute("info", "Tính năng đang phát triển");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
    /**
     * API trả về Fragment HTML chi tiết đơn hàng (Dùng cho AJAX Quick View)
     */
    @GetMapping("/quick-view/{id}")
    public String quickView(@PathVariable Long id, Model model) {
        Order order = orderService.findByIdWithDetails(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "id", id));

        model.addAttribute("order", com.argaty.dto.response.OrderResponse.fromEntity(order)); // Dùng OrderResponse cho đồng bộ
        model.addAttribute("orderStatuses", com.argaty.enums.OrderStatus.values());
        
        // Trả về cú pháp: "tên_file :: tên_fragment"
        // Nghĩa là: Vào file admin/orders/detail.html lấy phần có th:fragment="order-detail-content"
        return "admin/orders/detail :: order-detail-content"; 
    }
    @PostMapping("/api/orders/{id}/status")
    @ResponseBody // Quan trọng: Trả về JSON chứ không chuyển trang
    public org.springframework.http.ResponseEntity<?> updateStatusAjax(
            @PathVariable Long id,
            @RequestParam("status") com.argaty.enums.OrderStatus status,
            Principal principal) {
        try {
            User admin = userService.findByEmail(principal.getName()).orElseThrow();
            // Gọi service cập nhật như bình thường
            orderService.updateStatus(id, status, admin, null); // Note có thể null hoặc lấy từ request
            
            return org.springframework.http.ResponseEntity.ok()
                    .body(java.util.Map.of("success", true, "message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }
}