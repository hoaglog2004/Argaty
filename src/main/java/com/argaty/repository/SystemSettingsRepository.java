package com.argaty.repository;

import com.argaty.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho SystemSettings Entity
 */
@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, String> {

    Optional<SystemSettings> findBySettingKey(String settingKey);

    List<SystemSettings> findBySettingGroup(String settingGroup);
}
