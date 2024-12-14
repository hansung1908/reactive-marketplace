package com.hansung.reactive_marketplace.dto.response;

public record ProductUpdateResDto(
        String id,
        String title,
        int price,
        String description,
        String nickname,
        String imagePath) {}
