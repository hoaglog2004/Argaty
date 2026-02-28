package com.argaty.service;

import com.argaty.entity.Order;

/**
 * Service interface cho Email
 */
public interface EmailService {

    void sendEmail(String to, String subject, String content);

    void sendHtmlEmail(String to, String subject, String htmlContent);

    void sendPasswordResetEmail(String to, String token);

    void sendEmailVerificationEmail(String to, String token);

    void sendOrderConfirmationEmail(Order order);

    void sendOrderStatusUpdateEmail(Order order);

    void sendWelcomeEmail(String to, String fullName);

    void sendNewsletterSubscriptionEmail(String to);

    void sendContactMessageEmail(String name, String email, String phone, String subject, String message);
}