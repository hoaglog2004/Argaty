package com.argaty.service;

import com.argaty.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Banner
 */
public interface BannerService {

    Banner save(Banner banner);

    Optional<Banner> findById(Long id);

    Page<Banner> findAll(Pageable pageable);

    List<Banner> findActiveByPosition(String position);

    List<Banner> findByPosition(String position);

    void deleteById(Long id);

    Banner create(String title, String subtitle, String imageUrl, String link,
                  String position, Integer displayOrder,
                  LocalDateTime startDate, LocalDateTime endDate);

    Banner update(Long id, String title, String subtitle, String imageUrl, String link,
                  String position, Integer displayOrder,
                  LocalDateTime startDate, LocalDateTime endDate);

    void toggleActive(Long id);

    void updateDisplayOrder(Long id, int displayOrder);
}