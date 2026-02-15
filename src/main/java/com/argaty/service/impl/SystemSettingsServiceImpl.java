package com.argaty.service.impl;

import com.argaty.entity.SystemSettings;
import com.argaty.repository.SystemSettingsRepository;
import com.argaty.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation của SystemSettingsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private final SystemSettingsRepository settingsRepository;

    @Override
    @Transactional(readOnly = true)
    public String getSetting(String key, String defaultValue) {
        return settingsRepository.findBySettingKey(key)
                .map(SystemSettings::getSettingValue)
                .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getIntSetting(String key, Integer defaultValue) {
        try {
            String value = getSetting(key, null);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer setting for key {}: {}", key, e.getMessage());
            return defaultValue;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDecimalSetting(String key, BigDecimal defaultValue) {
        try {
            String value = getSetting(key, null);
            return value != null ? new BigDecimal(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid decimal setting for key {}: {}", key, e.getMessage());
            return defaultValue;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getBooleanSetting(String key, Boolean defaultValue) {
        String value = getSetting(key, null);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @Override
    public void setSetting(String key, String value, String group) {
        SystemSettings setting = settingsRepository.findBySettingKey(key)
                .orElse(SystemSettings.builder()
                        .settingKey(key)
                        .settingGroup(group)
                        .build());
        
        setting.setSettingValue(value);
        settingsRepository.save(setting);
        log.info("Updated setting: {} = {}", key, value);
    }

    @Override
    public void updateSettings(Map<String, String> settings, String group) {
        settings.forEach((key, value) -> {
            if (value != null && !value.trim().isEmpty()) {
                setSetting(key, value, group);
            }
        });
        log.info("Updated {} settings in group {}", settings.size(), group);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        Map<String, String> result = new HashMap<>();
        List<SystemSettings> settings = settingsRepository.findAll();
        settings.forEach(s -> result.put(s.getSettingKey(), s.getSettingValue()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getSettingsByGroup(String group) {
        Map<String, String> result = new HashMap<>();
        List<SystemSettings> settings = settingsRepository.findBySettingGroup(group);
        settings.forEach(s -> result.put(s.getSettingKey(), s.getSettingValue()));
        return result;
    }
}
