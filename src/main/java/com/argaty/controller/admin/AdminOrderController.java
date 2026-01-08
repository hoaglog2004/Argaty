package com.argaty.controller.admin;

import com.argaty.dto.request.UpdateOrderStatusRequest;
import com.argaty.dto.response.OrderDetailResponse;
import com.argaty.dto.response.OrderResponse;
import com.argaty.dto.response.PageResponse;
import com.argaty.entity.Order;
import com.argaty.entity.User;
import com.argaty.enums.OrderStatus;
import com.argaty.exception.BadRequestException;
import com.argaty.service.OrderService;
import com.argaty.service.UserService;
import com.argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controller quản lý đơn hàng (Admin)
 */
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * Danh sách đơn hàng
     */
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

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findByIdWithDetails(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Order", "id", id));

        model.addAttribute("order", OrderDetailResponse.fromEntity(order));
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("adminPage", "orders");

        return "admin/orders/detail";
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateOrderStatusRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User admin = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

            orderService.updateStatus(id, request.getStatus(), admin, request.getNote());
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công");

        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    /**
     * Xác nhận đơn hàng
     */
    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(principal.getName()).orElse(null);
            orderService.confirmOrder(id, admin);
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đơn hàng");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Giao hàng
     */
    @PostMapping("/{id}/ship")
    public String shipOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User admin = userService.findByEmail(principal.getName()).orElse(null);
            orderService.shipOrder(id, admin, note);
            redirectAttributes.addFlashAttribute("success", "Đã chuyển sang giao hàng");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Hoàn thành đơn hàng
     */
    @PostMapping("/{id}/complete")
    public String completeOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(principal.getName()).orElse(null);
            orderService.completeOrder(id, admin);
            redirectAttributes.addFlashAttribute("success", "Đã hoàn thành đơn hàng");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(
            @PathVariable Long id,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User admin = userService.findByEmail(principal.getName()).orElse(null);
            orderService.cancelOrder(id, admin, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}