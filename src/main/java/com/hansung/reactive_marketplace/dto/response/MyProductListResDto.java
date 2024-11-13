package com.hansung.reactive_marketplace.dto.response;

import com.hansung.reactive_marketplace.domain.ProductStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyProductListResDto {

    private String id;

    private String title;

    private String description;

    private int price;

    private ProductStatus status;

    private LocalDateTime createdAt;

    private String thumbnailPath;

    protected MyProductListResDto() {
    }

    public MyProductListResDto(String id, String title, String description, int price, ProductStatus status, LocalDateTime createdAt, String thumbnailPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.thumbnailPath = thumbnailPath;
    }
}
