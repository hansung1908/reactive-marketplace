package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class ChatSaveReqDto {

    private String msg;

    private String sender;

    private String receiver;

    private String roomId;

    protected ChatSaveReqDto() {
    }

    public ChatSaveReqDto(String msg, String sender, String receiver, String roomId) {
        this.msg = msg;
        this.sender = sender;
        this.receiver = receiver;
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "ChatSaveReqDto{" +
                "msg='" + msg + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
