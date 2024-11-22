package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;

public record ProductSaveReqDto(
        String title,
        String description,
        int price,
        ImageSource imageSource) {}
