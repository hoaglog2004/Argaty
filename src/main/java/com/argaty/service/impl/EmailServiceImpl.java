package com.argaty.service.impl;

import java.time.Year;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.argaty.entity.Order;
import com.argaty.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation của EmailService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@argaty.com}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.contact.to:support@argaty.com}")
    private String contactToEmail;

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        if (!isMailConfigured()) {
            log.warn("Skipped sending email to {} because SMTP is not configured", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Sent email to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!isMailConfigured()) {
            log.warn("Skipped sending HTML email to {} because SMTP is not configured", to);
            return;
        }

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

        String subject = "[Argaty] Đặt lại mật khẩu";
        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiryMinutes", 30);
        context.setVariable("year", Year.now().getValue());

        try {
            String htmlContent = templateEngine.process("email/password-reset-email", context);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to render password reset email template for {}: {}", to, e.getMessage());
            String fallbackContent = String.format(
                    "Xin chào,\n\n" +
                    "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Argaty.\n\n" +
                    "Vui lòng click vào link sau để đặt lại mật khẩu:\n%s\n\n" +
                    "Link này sẽ hết hạn sau 30 phút.\n\n" +
                    "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\nArgaty Team",
                    resetUrl
            );
            sendEmail(to, subject, fallbackContent);
        }
    }

    @Override
    @Async
    public void sendEmailVerificationEmail(String to, String token) {
        String verifyUrl = baseUrl + "/auth/verify-email?token=" + token;

        String subject = "[Argaty] Xác thực email";
        Context context = new Context();
        context.setVariable("verifyUrl", verifyUrl);
        context.setVariable("year", Year.now().getValue());

        try {
            String htmlContent = templateEngine.process("email/email-verification-email", context);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to render email verification template for {}: {}", to, e.getMessage());
            String fallbackContent = String.format(
                    "Xin chào,\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản Argaty.\n\n" +
                    "Vui lòng click vào link sau để xác thực email:\n%s\n\n" +
                    "Trân trọng,\nArgaty Team",
                    verifyUrl
            );
            sendEmail(to, subject, fallbackContent);
        }
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        String subject = String.format("[Argaty] Xác nhận đơn hàng #%s", order.getOrderCode());
        String email = order.getReceiverEmail() != null ? order.getReceiverEmail() : order.getUser().getEmail();
        Context context = new Context();
        context.setVariable("receiverName", order.getReceiverName());
        context.setVariable("orderCode", order.getOrderCode());
        context.setVariable("totalAmount", String.format("%,d", order.getTotalAmount().longValue()));
        context.setVariable("paymentMethod", order.getPaymentMethod().getDisplayName());
        context.setVariable("orderUrl", baseUrl + "/profile/orders/" + order.getOrderCode());
        context.setVariable("year", Year.now().getValue());

        try {
            String htmlContent = templateEngine.process("email/order-confirmation-email", context);
            sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to render order confirmation template for {}: {}", email, e.getMessage());
            String fallbackContent = String.format(
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
            sendEmail(email, subject, fallbackContent);
        }
    }

    @Override
    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        String subject = String.format("[Argaty] Cập nhật đơn hàng #%s", order.getOrderCode());
        String email = order.getReceiverEmail() != null ? order.getReceiverEmail() : order.getUser().getEmail();
        Context context = new Context();
        context.setVariable("receiverName", order.getReceiverName());
        context.setVariable("orderCode", order.getOrderCode());
        context.setVariable("statusDisplay", order.getStatus().getDisplayName());
        context.setVariable("orderUrl", baseUrl + "/profile/orders/" + order.getOrderCode());
        context.setVariable("year", Year.now().getValue());

        try {
            String htmlContent = templateEngine.process("email/order-status-update-email", context);
            sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to render order status update template for {}: {}", email, e.getMessage());
            String fallbackContent = String.format(
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
            sendEmail(email, subject, fallbackContent);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "[Argaty] Chào mừng bạn đến với Argaty!";
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("homeUrl", baseUrl);
        context.setVariable("year", Year.now().getValue());

        try {
            String htmlContent = templateEngine.process("email/welcome-email", context);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to render welcome email template for {}: {}", to, e.getMessage());
            String fallbackContent = String.format(
                    "Xin chào %s,\n\n" +
                    "Chào mừng bạn đến với Argaty - Thiên đường Gaming Gear!\n\n" +
                    "Tài khoản của bạn đã được tạo thành công.\n\n" +
                    "Khám phá ngay các sản phẩm gaming gear chất lượng cao tại:\n%s\n\n" +
                    "Trân trọng,\nArgaty Team",
                    fullName,
                    baseUrl
            );
            sendEmail(to, subject, fallbackContent);
        }
    }

    @Override
    @Async
    public void sendNewsletterSubscriptionEmail(String to) {
        String subject = "[Argaty] Đăng ký nhận tin thành công";
        Context context = new Context();
        context.setVariable("homeUrl", baseUrl);
        context.setVariable("year", Year.now().getValue());

        try {
            String htmlContent = templateEngine.process("email/newsletter-subscription-email", context);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to render newsletter subscription template for {}: {}", to, e.getMessage());
            String fallbackContent = "Xin chào,\n\n" +
                    "Bạn đã đăng ký nhận tin từ Argaty thành công.\n" +
                    "Chúng tôi sẽ gửi cho bạn các thông tin khuyến mãi và sản phẩm mới sớm nhất.\n\n" +
                    "Trân trọng,\nArgaty Team";
            sendEmail(to, subject, fallbackContent);
        }
    }

    @Override
    @Async
    public void sendContactMessageEmail(String name, String email, String phone, String subject, String message) {
        String adminSubject = "[Argaty] Liên hệ mới: " + subject;
        String adminContent = String.format(
                "Bạn có tin nhắn liên hệ mới từ website Argaty.\n\n" +
                "Họ tên: %s\n" +
                "Email: %s\n" +
                "Số điện thoại: %s\n" +
                "Chủ đề: %s\n\n" +
                "Nội dung:\n%s\n",
                name,
                email,
                StringUtils.hasText(phone) ? phone : "(không có)",
                subject,
                message
        );
        sendEmail(contactToEmail, adminSubject, adminContent);

        String customerSubject = "[Argaty] Chúng tôi đã nhận tin nhắn của bạn";
        String customerContent = String.format(
                "Xin chào %s,\n\n" +
                "Argaty đã nhận được liên hệ của bạn với chủ đề: %s.\n" +
                "Đội ngũ hỗ trợ sẽ phản hồi bạn sớm nhất có thể.\n\n" +
                "Trân trọng,\nArgaty Team",
                name,
                subject
        );
        sendEmail(email, customerSubject, customerContent);
    }

    private boolean isMailConfigured() {
        return StringUtils.hasText(mailUsername) && StringUtils.hasText(mailPassword);
    }
}