package com.hansung.reactive_marketplace.dto.response;

import java.time.LocalDateTime;

public record BuyerChatRoomListResDto(
        String productId,
        String productTitle,
        String sellerNickname,
        String buyerNickname,
        String recentMsg,
        LocalDateTime recentCreatedAt,
        String thumbnailPath
) {}
