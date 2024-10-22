package com.hansung.reactive_marketplace.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter // json 직렬화로 데이터 가져올때 사용
@Document(collection = "product")
public class Product {

    @Id
    private String id;

    private String title;

    private String description;

    private int price;

    private ProductStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    private String userId;

    protected Product() {
    }

    private Product(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.price = builder.price;
        this.status = ProductStatus.ON_SALE; // 초깃값은 ON_SALE로 자동 설정
        this.userId = builder.userId;
    }

    // 빌더 클래스
    public static class Builder {

        private String title;
        private String description;
        private int price;
        private String userId;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(int price) {
            this.price = price;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Product build() {
            return new Product(this); // Product 객체 생성
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", userId='" + userId + '\'' +
                '}';
    }
}
