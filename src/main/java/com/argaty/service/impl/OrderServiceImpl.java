package com.argaty.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.argaty.entity.Cart;
import com.argaty.entity.CartItem;
import com.argaty.entity.Order;
import com.argaty.entity.OrderItem;
import com.argaty.entity.User;
import com.argaty.entity.Voucher;
import com.argaty.enums.OrderStatus;
import com.argaty.enums.PaymentMethod;
import com.argaty.exception.BadRequestException;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.CartItemRepository;
import com.argaty.repository.CartRepository;
import com.argaty.repository.OrderItemRepository;
import com.argaty.repository.OrderRepository;
import com.argaty.repository.UserRepository;
import com.argaty.service.NotificationService;
import com.argaty.service.OrderService;
import com.argaty.service.ProductService;
import com.argaty.service.VoucherService;
import com.argaty.util.OrderCodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation của OrderService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final VoucherService voucherService;
    private final NotificationService notificationService;

    // ========== CRUD ==========

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByIdWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // ========== USER ORDERS ==========

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByUserIdAndStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId) {
        return orderRepository.findByOrderCodeAndUserId(orderCode, userId);
    }

    // ========== CREATE ORDER ==========

    @Override
    public Order createOrder(Long userId, String receiverName, String receiverPhone,
                             String receiverEmail, String shippingAddress,
                             String city, String district, String ward,
                             PaymentMethod paymentMethod, String voucherCode, String note) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Lấy giỏ hàng
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BadRequestException("Giỏ hàng trống"));

        List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelectedTrue(cart.getId());
        if (selectedItems.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn sản phẩm để đặt hàng");
        }

        return createOrderFromCartItems(user, selectedItems, receiverName, receiverPhone,
                receiverEmail, shippingAddress, city, district, ward,
                paymentMethod, voucherCode, note);
    }

    @Override
    public Order createOrderFromCart(Long userId, Long cartId, String receiverName,
                                     String receiverPhone, String receiverEmail,
                                     String shippingAddress, String city,
                                     String district, String ward,
                                     PaymentMethod paymentMethod, String voucherCode, String note) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelectedTrue(cartId);
        if (selectedItems.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn sản phẩm để đặt hàng");
        }

        return createOrderFromCartItems(user, selectedItems, receiverName, receiverPhone,
                receiverEmail, shippingAddress, city, district, ward,
                paymentMethod, voucherCode, note);
    }

    private Order createOrderFromCartItems(User user, List<CartItem> cartItems,
                                           String receiverName, String receiverPhone,
                                           String receiverEmail, String shippingAddress,
                                           String city, String district, String ward,
                                           PaymentMethod paymentMethod, String voucherCode, String note) {

        // Validate và tính tổng tiền
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            // Kiểm tra sản phẩm còn hàng
            if (!item.isInStock()) {
                throw new BadRequestException("Sản phẩm '" + item.getProduct().getName() +
                        "' không đủ số lượng tồn kho");
            }
            subtotal = subtotal.add(item.getSubtotal());
        }

        // Tính phí ship
        BigDecimal shippingFee = calculateShippingFee(subtotal, city);

        // Áp dụng voucher nếu có
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            if (!voucherService.canUserUseVoucher(voucherCode, user.getId())) {
                throw new BadRequestException("Mã voucher không hợp lệ hoặc đã hết lượt sử dụng");
            }
            discountAmount = voucherService.calculateDiscount(voucherCode, subtotal);
            voucher = voucherService.findByCode(voucherCode).orElse(null);
        }

        // Tính tổng thanh toán
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // Tạo đơn hàng
        Order order = Order.builder()
                .orderCode(OrderCodeGenerator.generate())
                .user(user)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverEmail(receiverEmail)
                .shippingAddress(shippingAddress)
                .city(city)
                .district(district)
                .ward(ward)
                .paymentMethod(paymentMethod)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .voucher(voucher)
                .voucherCode(voucherCode)
                .status(OrderStatus.PENDING)
                .note(note)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Tạo order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .variant(cartItem.getVariant())
                    .productName(cartItem.getProduct().getName())
                    .productImage(cartItem.getImage())
                    .variantName(cartItem.getVariant() != null ? cartItem.getVariant().getName() : null)
                    .sku(cartItem.getVariant() != null ? cartItem.getVariant().getSku() : cartItem.getProduct().getSku())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(cartItem.getSubtotal())
                    .build();

            orderItemRepository.save(orderItem);

            // Giảm tồn kho
            productService.decreaseStock(
                    cartItem.getProduct().getId(),
                    cartItem.getVariant() != null ? cartItem.getVariant().getId() : null,
                    cartItem.getQuantity()
            );
        }

        // Áp dụng voucher (đánh dấu đã sử dụng)
        if (voucher != null) {
            voucherService.applyVoucher(voucherCode, user.getId(), savedOrder.getId());
        }

        // Xóa items đã đặt hàng khỏi giỏ
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteSelectedItems(cart.getId());
        }

        // Gửi thông báo
        notificationService.sendOrderCreatedNotification(savedOrder);

        log.info("Created order: {} for user: {}", savedOrder.getOrderCode(), user.getId());
        return savedOrder;
    }

    private BigDecimal calculateShippingFee(BigDecimal subtotal, String city) {
        // Miễn phí ship cho đơn từ 500k
        if (subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO;
        }

        // Phí ship cơ bản
        return BigDecimal.valueOf(30000);
    }

    // ========== ORDER STATUS ==========

    @Override
    public Order updateStatus(Long orderId, OrderStatus newStatus, User changedBy, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(newStatus, changedBy, note);
        Order savedOrder = orderRepository.save(order);

        // Gửi thông báo
        notificationService.sendOrderStatusNotification(savedOrder, oldStatus, newStatus);

        log.info("Updated order {} status: {} -> {}", order.getOrderCode(), oldStatus, newStatus);
        return savedOrder;
    }

    @Override
    public Order confirmOrder(Long orderId, User changedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể xác nhận đơn hàng đang chờ xử lý");
        }

        return updateStatus(orderId, OrderStatus.CONFIRMED, changedBy, "Đơn hàng đã được xác nhận");
    }

    @Override
    public Order shipOrder(Long orderId, User changedBy, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException("Đơn hàng chưa được xác nhận");
        }

        return updateStatus(orderId, OrderStatus.SHIPPING, changedBy, note != null ? note : "Đơn hàng đang được giao");
    }

    @Override
    public Order deliverOrder(Long orderId, User changedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new BadRequestException("Đơn hàng chưa được giao");
        }

        return updateStatus(orderId, OrderStatus.DELIVERED, changedBy, "Đơn hàng đã được giao thành công");
    }

    @Override
    public Order completeOrder(Long orderId, User changedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Đơn hàng chưa được giao");
        }

        // Đánh dấu đã thanh toán nếu là COD
        if (order.getPaymentMethod() == PaymentMethod.COD && !order.getIsPaid()) {
            order.setIsPaid(true);
            order.setPaidAt(LocalDateTime.now());
        }

        return updateStatus(orderId, OrderStatus.COMPLETED, changedBy, "Đơn hàng hoàn thành");
    }

    @Override
    public Order cancelOrder(Long orderId, User changedBy, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canCancel()) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái này");
        }

        order.setCancelReason(reason);

        // Hoàn lại tồn kho
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.increaseStock(
                    item.getProduct().getId(),
                    item.getVariant() != null ? item.getVariant().getId() : null,
                    item.getQuantity()
            );
        }

        return updateStatus(orderId, OrderStatus.CANCELLED, changedBy, "Đơn hàng đã bị hủy: " + reason);
    }

    @Override
    public Order requestReturn(Long orderId, User user, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Kiểm tra quyền
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền thao tác đơn hàng này");
        }

        if (!order.canRequestReturn()) {
            throw new BadRequestException("Không thể yêu cầu đổi trả ở trạng thái này");
        }

        order.setReturnReason(reason);
        return updateStatus(orderId, OrderStatus.RETURN_REQUESTED, user, "Yêu cầu đổi trả: " + reason);
    }

    @Override
    public Order approveReturn(Long orderId, User changedBy, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new BadRequestException("Đơn hàng chưa có yêu cầu đổi trả");
        }

        // Hoàn lại tồn kho
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.increaseStock(
                    item.getProduct().getId(),
                    item.getVariant() != null ? item.getVariant().getId() : null,
                    item.getQuantity()
            );
        }

        return updateStatus(orderId, OrderStatus.RETURNED, changedBy, note);
    }

    // ========== PAYMENT ==========

    @Override
    public Order updatePaymentStatus(Long orderId, boolean isPaid, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setIsPaid(isPaid);
        if (isPaid) {
            order.setPaidAt(LocalDateTime.now());
            order.setPaymentTransactionId(transactionId);
        }

        log.info("Updated payment status for order {}: paid={}", order.getOrderCode(), isPaid);
        return orderRepository.save(order);
    }

    // ========== SEARCH & FILTER ==========

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> searchOrders(String keyword, Pageable pageable) {
        return orderRepository.searchOrders(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByDateRange(startDate, endDate, pageable);
    }

    // ========== STATISTICS ==========

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersToday() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        return orderRepository.countOrdersFromDate(startOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        return orderRepository.countOrdersFromDate(startOfMonth);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueToday() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        BigDecimal revenue = orderRepository.getRevenueFromDate(startOfDay);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        BigDecimal revenue = orderRepository.getRevenueFromDate(startOfMonth);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpentByUser(Long userId) {
        BigDecimal spent = orderRepository.getTotalSpentByUser(userId);
        return spent != null ? spent : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDailyStatistics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.getDailyStatistics(startDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyStatistics() {
        return orderRepository.getMonthlyStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopSellingProducts(int limit) {
        return orderItemRepository.getTopSellingProducts(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopCustomers(int limit) {
        return orderRepository.getTopCustomers(PageRequest.of(0, limit));
    }
}