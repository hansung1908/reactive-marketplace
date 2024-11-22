package com.hansung.reactive_marketplace.dto.response;

public record ProductListResDto(
        String id,
        String title,
        int price,
        String thumbnailPath) {}
