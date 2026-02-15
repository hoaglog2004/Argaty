package com.argaty.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.argaty.dto.response.SettingsResponse;
import com.argaty.service.SystemSettingsService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final SystemSettingsService settingsService;

    @GetMapping("/settings")
    public String settings(Model model) {
        // Load settings from database
        SettingsResponse settings = SettingsResponse.builder()
                .storeName(settingsService.getSetting("store.name", "Argaty - Gaming Gear"))
                .contactEmail(settingsService.getSetting("contact.email", "support@argaty.com"))
                .contactPhone(settingsService.getSetting("contact.phone", "1900 123 456"))
                .address(settingsService.getSetting("store.address", ""))
                .seoDescription(settingsService.getSetting("seo.description", ""))
                .defaultShippingFee(settingsService.getDecimalSetting("shipping.default_fee", BigDecimal.valueOf(30000)))
                .freeShippingThreshold(settingsService.getDecimalSetting("shipping.free_threshold", BigDecimal.valueOf(500000)))
                .estimatedDeliveryDays(settingsService.getIntSetting("shipping.delivery_days", 3))
                .codEnabled(settingsService.getBooleanSetting("payment.cod_enabled", true))
                .onlinePaymentEnabled(settingsService.getBooleanSetting("payment.online_enabled", true))
                .smtpHost(settingsService.getSetting("email.smtp_host", ""))
                .smtpPort(settingsService.getIntSetting("email.smtp_port", 587))
                .smtpUsername(settingsService.getSetting("email.smtp_username", ""))
                .smtpTls(settingsService.getBooleanSetting("email.smtp_tls", true))
                .build();

        model.addAttribute("settings", settings);
        model.addAttribute("adminPage", "settings");
        return "admin/settings";
    }

    @PostMapping("/settings/general")
    public String updateGeneralSettings(
            @RequestParam String storeName,
            @RequestParam String contactEmail,
            @RequestParam String contactPhone,
            @RequestParam String address,
            @RequestParam String seoDescription,
            RedirectAttributes redirectAttributes) {
        
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("store.name", storeName);
            settings.put("contact.email", contactEmail);
            settings.put("contact.phone", contactPhone);
            settings.put("store.address", address);
            settings.put("seo.description", seoDescription);
            
            settingsService.updateSettings(settings, "general");
            redirectAttributes.addFlashAttribute("success", "Cập nhật cài đặt chung thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/shipping")
    public String updateShippingSettings(
            @RequestParam BigDecimal defaultShippingFee,
            @RequestParam BigDecimal freeShippingThreshold,
            @RequestParam Integer estimatedDeliveryDays,
            RedirectAttributes redirectAttributes) {
        
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("shipping.default_fee", defaultShippingFee.toString());
            settings.put("shipping.free_threshold", freeShippingThreshold.toString());
            settings.put("shipping.delivery_days", estimatedDeliveryDays.toString());
            
            settingsService.updateSettings(settings, "shipping");
            redirectAttributes.addFlashAttribute("success", "Cập nhật cài đặt vận chuyển thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/payment")
    public String updatePaymentSettings(
            @RequestParam(required = false, defaultValue = "false") Boolean codEnabled,
            @RequestParam(required = false, defaultValue = "false") Boolean onlinePaymentEnabled,
            RedirectAttributes redirectAttributes) {
        
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("payment.cod_enabled", codEnabled.toString());
            settings.put("payment.online_enabled", onlinePaymentEnabled.toString());
            
            settingsService.updateSettings(settings, "payment");
            redirectAttributes.addFlashAttribute("success", "Cập nhật cài đặt thanh toán thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/email")
    public String updateEmailSettings(
            @RequestParam String smtpHost,
            @RequestParam Integer smtpPort,
            @RequestParam String smtpUsername,
            @RequestParam(required = false) String smtpPassword,
            @RequestParam(required = false, defaultValue = "false") Boolean smtpTls,
            RedirectAttributes redirectAttributes) {
        
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("email.smtp_host", smtpHost);
            settings.put("email.smtp_port", smtpPort.toString());
            settings.put("email.smtp_username", smtpUsername);
            if (smtpPassword != null && !smtpPassword.trim().isEmpty()) {
                settings.put("email.smtp_password", smtpPassword);
            }
            settings.put("email.smtp_tls", smtpTls.toString());
            
            settingsService.updateSettings(settings, "email");
            redirectAttributes.addFlashAttribute("success", "Cập nhật cài đặt email thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
