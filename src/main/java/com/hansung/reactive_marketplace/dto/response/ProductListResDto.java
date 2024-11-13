package com.hansung.reactive_marketplace.dto.response;

import lombok.Getter;

@Getter
public class ProductListResDto {

    private String id;

    private String title;

    private int price;

    private String thumbnailPath;

    protected ProductListResDto() {
    }

    public ProductListResDto(String id, String title, int price, String thumbnailPath) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.thumbnailPath = thumbnailPath;
    }
}
