package com.hansung.reactive_marketplace.dto.response;

import com.hansung.reactive_marketplace.domain.ProductStatus;

import java.time.LocalDateTime;

public record MyProductListResDto(
        String id,
        String title,
        String description,
        int price,
        ProductStatus status,
        LocalDateTime createdAt,
        String thumbnailPath) {}