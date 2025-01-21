package com.hansung.reactive_marketplace.dto.response;

import com.hansung.reactive_marketplace.domain.ProductStatus;

public record MyProductListResDto(
        String id,
        String title,
        String description,
        int price,
        ProductStatus status,
        String createdAt,
        String thumbnailPath) {}