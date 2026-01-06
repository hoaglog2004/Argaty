package com. argaty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO cho response thống kê đánh giá sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class ReviewStatsResponse {

    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // 1->5 stars:  count
    private Map<Integer, Integer> ratingPercentage; // 1->5 stars: percentage

    public static ReviewStatsResponse create(Double avgRating, Long total, Map<Integer, Long> distribution) {
        Map<Integer, Integer> percentages = new java.util.HashMap<>();
        
        if (total > 0) {
            for (int i = 1; i <= 5; i++) {
                long count = distribution.getOrDefault(i, 0L);
                int percentage = (int) Math.round((count * 100.0) / total);
                percentages.put(i, percentage);
            }
        }

        return ReviewStatsResponse.builder()
                .averageRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0)
                .totalReviews(total)
                .ratingDistribution(distribution)
                .ratingPercentage(percentages)
                .build();
    }
}