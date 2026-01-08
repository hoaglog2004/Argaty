package com.argaty.service.impl;

import com.argaty.entity.Banner;
import com.argaty.exception.ResourceNotFoundException;
import com.argaty.repository.BannerRepository;
import com.argaty.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của BannerService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public Banner save(Banner banner) {
        return bannerRepository.save(banner);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Banner> findById(Long id) {
        return bannerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Banner> findAll(Pageable pageable) {
        return bannerRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> findActiveByPosition(String position) {
        return bannerRepository.findActiveBannersByPosition(position, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> findByPosition(String position) {
        return bannerRepository.findByPosition(position);
    }

    @Override
    public void deleteById(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banner", "id", id);
        }
        bannerRepository.deleteById(id);
        log.info("Deleted banner: {}", id);
    }

    @Override
    public Banner create(String title, String subtitle, String imageUrl, String link,
                         String position, Integer displayOrder,
                         LocalDateTime startDate, LocalDateTime endDate) {

        Banner banner = Banner.builder()
                .title(title)
                .subtitle(subtitle)
                .imageUrl(imageUrl)
                .link(link)
                .position(position != null ? position : Banner.POSITION_HOME_SLIDER)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .isActive(true)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        log.info("Created banner: {}", title);
        return bannerRepository.save(banner);
    }

    @Override
    public Banner update(Long id, String title, String subtitle, String imageUrl, String link,
                         String position, Integer displayOrder,
                         LocalDateTime startDate, LocalDateTime endDate) {

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", "id", id));

        banner.setTitle(title);
        banner.setSubtitle(subtitle);
        if (imageUrl != null) {
            banner.setImageUrl(imageUrl);
        }
        banner.setLink(link);
        if (position != null) {
            banner.setPosition(position);
        }
        if (displayOrder != null) {
            banner.setDisplayOrder(displayOrder);
        }
        banner.setStartDate(startDate);
        banner.setEndDate(endDate);

        log.info("Updated banner: {}", id);
        return bannerRepository.save(banner);
    }

    @Override
    public void toggleActive(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", "id", id));

        banner.setIsActive(!banner.getIsActive());
        bannerRepository.save(banner);
        log.info("Toggled banner active status: {} -> {}", id, banner.getIsActive());
    }

    @Override
    public void updateDisplayOrder(Long id, int displayOrder) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", "id", id));

        banner.setDisplayOrder(displayOrder);
        bannerRepository.save(banner);
    }
}