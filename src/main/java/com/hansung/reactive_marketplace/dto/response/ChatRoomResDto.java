package com.hansung.reactive_marketplace.dto.response;

import lombok.Getter;

@Getter
public class ChatRoomResDto {

    private String id;

    private String seller;

    private String buyer;

    private String thumbnailPath;

    protected ChatRoomResDto() {
    }

    public ChatRoomResDto(String id, String seller, String buyer, String thumbnailPath) {
        this.id = id;
        this.seller = seller;
        this.buyer = buyer;
        this.thumbnailPath = thumbnailPath;
    }
}
