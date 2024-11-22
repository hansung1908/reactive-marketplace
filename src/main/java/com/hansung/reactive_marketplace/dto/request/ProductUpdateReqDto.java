package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.ProductStatus;

public record ProductUpdateReqDto(
        String id,
        String description,
        int price,
        ProductStatus status,
        ImageSource imageSource) {}
