package com.hansung.reactive_marketplace.dto.request;

public record ChatSaveReqDto(
        String msg,
        String senderId,
        String receiverId,
        String roomId) {}
