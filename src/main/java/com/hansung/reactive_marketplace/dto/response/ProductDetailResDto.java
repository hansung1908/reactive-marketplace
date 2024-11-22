package com.hansung.reactive_marketplace.dto.response;

public record ProductDetailResDto(
        String id,
        String title,
        int price,
        String description,
        String userId,
        String nickname,
        String imagePath) {}
