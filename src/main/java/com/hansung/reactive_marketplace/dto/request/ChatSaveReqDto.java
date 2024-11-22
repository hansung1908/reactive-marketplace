package com.hansung.reactive_marketplace.dto.request;

public record ChatSaveReqDto(
        String msg,
        String sender,
        String receiver,
        String roomId) {}
