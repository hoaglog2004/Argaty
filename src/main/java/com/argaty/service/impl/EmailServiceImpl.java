package com.argaty.service.impl;

import com.argaty.entity.Order;
import com.argaty.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation của EmailService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@argaty.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Sent email to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent HTML email to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = baseUrl + "/auth/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiryMinutes", 30);

        String subject = "[Argaty] Đặt lại mật khẩu";
        String content = String.format(
                "Xin chào,\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Argaty.\n\n" +
                "Vui lòng click vào link sau để đặt lại mật khẩu:\n%s\n\n" +
                "Link này sẽ hết hạn sau 30 phút.\n\n" +
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\nArgaty Team",
                resetUrl
        );

        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendEmailVerificationEmail(String to, String token) {
        String verifyUrl = baseUrl + "/auth/verify-email?token=" + token;

        String subject = "[Argaty] Xác thực email";
        String content = String.format(
                "Xin chào,\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản Argaty.\n\n" +
                "Vui lòng click vào link sau để xác thực email:\n%s\n\n" +
                "Trân trọng,\nArgaty Team",
                verifyUrl
        );

        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        String subject = String.format("[Argaty] Xác nhận đơn hàng #%s", order.getOrderCode());
        String content = String.format(
                "Xin chào %s,\n\n" +
                "Cảm ơn bạn đã đặt hàng tại Argaty!\n\n" +
                "Mã đơn hàng:  %s\n" +
                "Tổng tiền: %,d VNĐ\n" +
                "Phương thức thanh toán:  %s\n\n" +
                "Bạn có thể theo dõi đơn hàng tại:\n%s/profile/orders/%s\n\n" +
                "Trân trọng,\nArgaty Team",
                order.getReceiverName(),
                order.getOrderCode(),
                order.getTotalAmount().longValue(),
                order.getPaymentMethod().getDisplayName(),
                baseUrl,
                order.getOrderCode()
        );

        String email = order.getReceiverEmail() != null ? order.getReceiverEmail() : order.getUser().getEmail();
        sendEmail(email, subject, content);
    }

    @Override
    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        String subject = String.format("[Argaty] Cập nhật đơn hàng #%s", order.getOrderCode());
        String content = String.format(
                "Xin chào %s,\n\n" +
                "Đơn hàng #%s của bạn đã được cập nhật.\n\n" +
                "Trạng thái mới:  %s\n\n" +
                "Bạn có thể theo dõi đơn hàng tại:\n%s/profile/orders/%s\n\n" +
                "Trân trọng,\nArgaty Team",
                order.getReceiverName(),
                order.getOrderCode(),
                order.getStatus().getDisplayName(),
                baseUrl,
                order.getOrderCode()
        );

        String email = order.getReceiverEmail() != null ? order.getReceiverEmail() : order.getUser().getEmail();
        sendEmail(email, subject, content);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "[Argaty] Chào mừng bạn đến với Argaty!";
        String content = String.format(
                "Xin chào %s,\n\n" +
                "Chào mừng bạn đến với Argaty - Thiên đường Gaming Gear!\n\n" +
                "Tài khoản của bạn đã được tạo thành công.\n\n" +
                "Khám phá ngay các sản phẩm gaming gear chất lượng cao tại:\n%s\n\n" +
                "Trân trọng,\nArgaty Team",
                fullName,
                baseUrl
        );

        sendEmail(to, subject, content);
    }
}