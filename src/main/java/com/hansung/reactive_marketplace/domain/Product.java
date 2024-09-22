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
    private ProductStatus status; // 초깃값은 ON_SALE로 자동 설정
    @CreatedDate
    private LocalDateTime created_at;

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", created_at=" + created_at +
                '}';
    }
}
