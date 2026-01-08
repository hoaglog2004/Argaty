package com.argaty.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity Order - Đơn hàng
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 20)
    private String orderCode;

    // ========== THÔNG TIN NGƯỜI NHẬN ==========

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 15)
    private String receiverPhone;

    @Column(name = "receiver_email", length = 100)
    private String receiverEmail;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "ward", length = 100)
    private String ward;

    // ========== THANH TOÁN ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    // ========== GIÁ TRỊ ĐƠN HÀNG ==========

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 0)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    // ========== TRẠNG THÁI ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    // ========== THỜI GIAN THEO DÕI ==========

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Lấy địa chỉ đầy đủ
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(shippingAddress);

        if (ward != null && !ward.isEmpty()) {
            sb.append(", ").append(ward);
        }
        sb.append(", ").append(district);
        sb.append(", ").append(city);

        return sb.toString();
    }

    /**
     * Đếm số lượng sản phẩm
     */
    public int getTotalItemCount() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Kiểm tra có thể hủy đơn
     */
    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Kiểm tra có thể yêu cầu đổi trả
     */
    public boolean canRequestReturn() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED;
    }

    /**
     * Kiểm tra đơn hàng đã hoàn thành
     */
    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    /**
     * Kiểm tra đơn hàng đã hủy
     */
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    /**
     * Thêm item vào đơn hàng
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    public void updateStatus(OrderStatus newStatus, User changedBy, String note) {
        this.status = newStatus;

        // Cập nhật thời gian tương ứng
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED -> this.confirmedAt = now;
            case SHIPPING -> this.shippedAt = now;
            case DELIVERED -> this.deliveredAt = now;
            case COMPLETED -> this.completedAt = now;
            case CANCELLED -> this.cancelledAt = now;
        }

        // Thêm vào lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .status(newStatus)
                .note(note)
                .changedBy(changedBy)
                .build();
        statusHistory.add(history);
    }
}