package com.argaty.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniCartItemResponse {
    private String name;
    private String slug;
    private String imageUrl;
    private int quantity;
    private BigDecimal price;
}