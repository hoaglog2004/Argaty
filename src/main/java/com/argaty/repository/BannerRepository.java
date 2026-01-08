package com.argaty.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.Banner;

/**
 * Repository cho Banner Entity
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByPositionAndIsActiveTrueOrderByDisplayOrderAsc(String position);

    @Query("SELECT b FROM Banner b WHERE b.position = :position AND b.isActive = true AND " +
           "(b.startDate IS NULL OR b.startDate <= :now) AND " +
           "(b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.displayOrder ASC")
    List<Banner> findActiveBannersByPosition(@Param("position") String position, 
                                              @Param("now") LocalDateTime now);

    Page<Banner> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT b FROM Banner b WHERE b.position = :position ORDER BY b.displayOrder ASC")
    List<Banner> findByPosition(@Param("position") String position);

    @Query("SELECT b FROM Banner b WHERE b.endDate < CURRENT_TIMESTAMP AND b.isActive = true")
    List<Banner> findExpiredBanners();
}