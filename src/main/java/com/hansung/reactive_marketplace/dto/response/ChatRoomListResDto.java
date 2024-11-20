package com.hansung.reactive_marketplace.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatRoomListResDto {

    private String productId;

    private String productTitle;

    private String seller;

    private String buyer;

    private String recentMsg;

    private LocalDateTime recentCreatedAt;

    private String thumbnailPath;

    protected ChatRoomListResDto() {
    }

    public ChatRoomListResDto(String productId, String productTitle, String seller, String buyer, String recentMsg, LocalDateTime recentCreatedAt, String thumbnailPath) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.seller = seller;
        this.buyer = buyer;
        this.recentMsg = recentMsg;
        this.recentCreatedAt = recentCreatedAt;
        this.thumbnailPath = thumbnailPath;
    }
}
