package com.hansung.reactive_marketplace.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat")
public class Chat {

    @Id
    private String id;

    private String msg;

    private String sender;

    private String receiver;

    private String roomId;

    @CreatedDate
    private LocalDateTime createdAt;

    protected Chat() {
    }

    public Chat(Builder builder) {
        this.msg = builder.msg;
        this.sender = builder.sender;
        this.receiver = builder.receiver;
        this.roomId = builder.roomId;
    }

    public static class Builder {
        private String msg;
        private String sender;
        private String receiver;
        private String roomId;

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder receiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public Chat build() {
            return new Chat(this);
        }
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", msg='" + msg + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", roomId='" + roomId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
