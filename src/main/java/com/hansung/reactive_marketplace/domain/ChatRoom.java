package com.hansung.reactive_marketplace.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatRoom")
public class ChatRoom {

    @Id
    private String id;

    private String productId;

    private String sellerId;

    private String buyerId;

    protected ChatRoom() {
    }

    public ChatRoom(Builder builder) {
        this.productId = builder.productId;
        this.sellerId = builder.sellerId;
        this.buyerId = builder.buyerId;
    }

    public static class Builder {
        private String productId;
        private String sellerId;
        private String buyerId;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder sellerId(String sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder buyerId(String buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public ChatRoom build() {
            return new ChatRoom(this);
        }
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", buyerId='" + buyerId + '\'' +
                '}';
    }
}
