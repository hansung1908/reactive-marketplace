package com.hansung.reactive_marketplace.dto.response;

public record ChatRoomResDto(
        String id,
        String sellerId,
        String buyerId,
        String thumbnailPath) {}
