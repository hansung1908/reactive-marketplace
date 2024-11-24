package com.hansung.reactive_marketplace.dto.response;

import java.time.LocalDateTime;

public record ChatRoomListResDto(
        String productId,
        String productTitle,
        String sellerId,
        String sellerNickname,
        String buyerId,
        String buyerNickname,
        String recentMsg,
        LocalDateTime recentCreatedAt,
        String thumbnailPath) {}
