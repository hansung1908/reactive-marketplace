package com.hansung.reactive_marketplace.dto.response;

public record ChatRoomListResDto (
        String productId,
        String productTitle,
        String sellerId,
        String sellerNickname,
        String buyerId,
        String buyerNickname,
        String recentMsg,
        String recentCreatedAt,
        String thumbnailPath
) {}
