package com.argaty.controller.admin;

import com.argaty.dto.request.ReviewReplyRequest;
import com.argaty.entity.Review;
import com.argaty.entity.User;
import com.argaty.service.ReviewService;
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

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    // --- 1. SỬA HÀM LIST ĐỂ HỖ TRỢ TÌM KIẾM ---
    @GetMapping
    public String list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String keyword, // Thêm tìm kiếm text
            @RequestParam(required = false) Integer rating, // Thêm lọc sao
            @RequestParam(required = false) String status,  // Thêm lọc trạng thái
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Gọi hàm search tổng hợp trong Service (Sẽ viết ở Bước 2)
        Page<Review> reviews = reviewService.searchReviews(productId, keyword, rating, status, pageRequest);

        model.addAttribute("reviews", DtoMapper.toReviewPageResponse(reviews));
        
        // Giữ lại các giá trị filter để hiển thị trên giao diện
        model.addAttribute("productId", productId);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("adminPage", "reviews");

        return "admin/reviews/list";
    }

    // ... (Hàm detail giữ nguyên) ...
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Review", "id", id));

        model.addAttribute("review", DtoMapper.toReviewResponse(review));
        model.addAttribute("replyRequest", new ReviewReplyRequest());
        model.addAttribute("adminPage", "reviews");

        return "admin/reviews/detail";
    }

    // ... (Hàm reply giữ nguyên) ...
    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id, 
                        @Valid @ModelAttribute ReviewReplyRequest request,
                        Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(principal.getName()).orElseThrow();
            reviewService.replyToReview(id, admin.getId(), request.getReply());
            redirectAttributes.addFlashAttribute("success", "Đã phản hồi đánh giá");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews/" + id;
    }

    // --- 2. THÊM HÀM APPROVE (DUYỆT) ---
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.approveReview(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đánh giá");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    // --- 3. THÊM HÀM REJECT (TỪ CHỐI) ---
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.rejectReview(id);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối đánh giá");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    // ... (Hàm delete giữ nguyên) ...
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa đánh giá");
        }
        return "redirect:/admin/reviews";
    }
}