package com.hansung.reactive_marketplace.dto.response;

public record ChatRoomResDto(
        String id,
        String senderId,
        String receiverId,
        String receiverNickname,
        String receiverThumbnailPath) {}
