package com.argaty.config;

import com.argaty.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Initialize default system settings if they don't exist
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemSettingsInitializer implements CommandLineRunner {

    private final SystemSettingsService settingsService;

    @Override
    public void run(String... args) {
        try {
            initializeDefaultSettings();
            log.info("System settings initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize system settings: {}", e.getMessage());
        }
    }

    private void initializeDefaultSettings() {
        // Only set if not already exists (getSetting will return default if not found)
        Map<String, String> defaultSettings = new HashMap<>();
        
        // General settings
        if (settingsService.getSetting("store.name", null) == null) {
            defaultSettings.put("store.name", "Argaty - Gaming Gear");
        }
        if (settingsService.getSetting("contact.email", null) == null) {
            defaultSettings.put("contact.email", "support@argaty.com");
        }
        if (settingsService.getSetting("contact.phone", null) == null) {
            defaultSettings.put("contact.phone", "1900 123 456");
        }
        if (settingsService.getSetting("store.address", null) == null) {
            defaultSettings.put("store.address", "123 Nguyễn Văn Cừ, Quận 1, TP. Hồ Chí Minh");
        }
        if (settingsService.getSetting("seo.description", null) == null) {
            defaultSettings.put("seo.description", "Cửa hàng gaming gear chuyên nghiệp");
        }

        // Shipping settings
        if (settingsService.getSetting("shipping.default_fee", null) == null) {
            defaultSettings.put("shipping.default_fee", "30000");
        }
        if (settingsService.getSetting("shipping.free_threshold", null) == null) {
            defaultSettings.put("shipping.free_threshold", "500000");
        }
        if (settingsService.getSetting("shipping.delivery_days", null) == null) {
            defaultSettings.put("shipping.delivery_days", "3");
        }

        // Payment settings
        if (settingsService.getSetting("payment.cod_enabled", null) == null) {
            defaultSettings.put("payment.cod_enabled", "true");
        }
        if (settingsService.getSetting("payment.online_enabled", null) == null) {
            defaultSettings.put("payment.online_enabled", "true");
        }

        // Email settings
        if (settingsService.getSetting("email.smtp_host", null) == null) {
            defaultSettings.put("email.smtp_host", "smtp.gmail.com");
        }
        if (settingsService.getSetting("email.smtp_port", null) == null) {
            defaultSettings.put("email.smtp_port", "587");
        }
        if (settingsService.getSetting("email.smtp_tls", null) == null) {
            defaultSettings.put("email.smtp_tls", "true");
        }

        if (!defaultSettings.isEmpty()) {
            defaultSettings.forEach((key, value) -> {
                String group = key.substring(0, key.indexOf('.'));
                settingsService.setSetting(key, value, group);
            });
            log.info("Initialized {} default settings", defaultSettings.size());
        }
    }
}
