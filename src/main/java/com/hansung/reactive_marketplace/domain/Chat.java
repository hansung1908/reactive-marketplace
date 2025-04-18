package com.hansung.reactive_marketplace.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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

    private String senderId;

    private String receiverId;

    private String roomId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @CreatedDate
    private LocalDateTime createdAt;

    protected Chat() {
    }

    public Chat(Builder builder) {
        this.msg = builder.msg;
        this.senderId = builder.senderId;
        this.receiverId = builder.receiverId;
        this.roomId = builder.roomId;
    }

    public static class Builder {
        private String msg;
        private String senderId;
        private String receiverId;
        private String roomId;

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder senderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder receiverId(String receiverId) {
            this.receiverId = receiverId;
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
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
