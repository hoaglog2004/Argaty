package com.argaty.controller.admin;

import com.argaty.dto.request.ReviewReplyRequest;
import com.argaty.entity.Review;
import com.argaty.entity.User;
import com.argaty. service.ReviewService;
import com. argaty.service.UserService;
import com.argaty.util.DtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data. domain.Page;
import org. springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controller quản lý đánh giá (Admin)
 */
@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    /**
     * Danh sách đánh giá
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest. of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviews;
        if (productId != null) {
            reviews = reviewService.findByProductId(productId, pageRequest);
            model.addAttribute("productId", productId);
        } else {
            // Lấy tất cả reviews - cần thêm method trong service
            reviews = reviewService. findByProductId(null, pageRequest);
        }

        model.addAttribute("reviews", DtoMapper.toReviewPageResponse(reviews));
        model.addAttribute("adminPage", "reviews");

        return "admin/reviews/list";
    }

    /**
     * Chi tiết đánh giá
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("Review", "id", id));

        model.addAttribute("review", DtoMapper. toReviewResponse(review));
        model.addAttribute("replyRequest", new ReviewReplyRequest());
        model.addAttribute("adminPage", "reviews");

        return "admin/reviews/detail";
    }

    /**
     * Phản hồi đánh giá
     */
    @PostMapping("/{id}/reply")
    public String reply(
            @PathVariable Long id,
            @Valid @ModelAttribute ReviewReplyRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User admin = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new com.argaty.exception.ResourceNotFoundException("User", "email", principal.getName()));

            reviewService.replyToReview(id, admin.getId(), request.getReply());
            redirectAttributes.addFlashAttribute("success", "Đã phản hồi đánh giá");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews/" + id;
    }

    /**
     * Toggle visibility
     */
    @PostMapping("/{id}/toggle-visibility")
    public String toggleVisibility(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.toggleVisibility(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái hiển thị");
        return "redirect:/admin/reviews";
    }

    /**
     * Xóa đánh giá
     */
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