package com.hansung.reactive_marketplace.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "product")
public class Product {

    @Id
    private int id;
    private String title;
    private String description;
    private int price;
    private ProductStatus status; // 초깃값은 on_sale로 자동 설정
    @CreatedDate
    private LocalDateTime created_at;
}
