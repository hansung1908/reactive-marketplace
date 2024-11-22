package com.hansung.reactive_marketplace.dto.response;

import java.time.LocalDateTime;

public record ChatRoomListResDto(
        String productId,
        String productTitle,
        String seller,
        String buyer,
        String recentMsg,
        LocalDateTime recentCreatedAt,
        String thumbnailPath) {}
