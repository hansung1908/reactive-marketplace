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

    private String seller;

    private String buyer;

    protected ChatRoom() {
    }

    public ChatRoom(Builder builder) {
        this.productId = builder.productId;
        this.seller = builder.seller;
        this.buyer = builder.buyer;
    }

    public static class Builder {
        private String productId;
        private String seller;
        private String buyer;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder seller(String seller) {
            this.seller = seller;
            return this;
        }

        public Builder buyer(String buyer) {
            this.buyer = buyer;
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
                ", seller='" + seller + '\'' +
                ", buyer='" + buyer + '\'' +
                '}';
    }
}
