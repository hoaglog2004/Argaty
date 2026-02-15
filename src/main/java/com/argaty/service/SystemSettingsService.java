package com.argaty.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service interface cho SystemSettings
 */
public interface SystemSettingsService {

    String getSetting(String key, String defaultValue);

    Integer getIntSetting(String key, Integer defaultValue);

    BigDecimal getDecimalSetting(String key, BigDecimal defaultValue);

    Boolean getBooleanSetting(String key, Boolean defaultValue);

    void setSetting(String key, String value, String group);

    void updateSettings(Map<String, String> settings, String group);

    Map<String, String> getAllSettings();

    Map<String, String> getSettingsByGroup(String group);
}
