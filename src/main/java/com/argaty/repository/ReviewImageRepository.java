package com.argaty.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.argaty.entity.ReviewImage;

/**
 * Repository cho ReviewImage Entity
 */
@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findByReviewId(Long reviewId);

    @Modifying
    @Query("DELETE FROM ReviewImage ri WHERE ri.review.id = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);

    int countByReviewId(Long reviewId);
}