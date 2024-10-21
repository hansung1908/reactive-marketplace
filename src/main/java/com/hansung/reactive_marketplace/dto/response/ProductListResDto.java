package com.hansung.reactive_marketplace.dto.response;

import lombok.Getter;

@Getter
public class ProductListResDto {

    private String id;

    private String title;

    private int price;

    protected ProductListResDto() {
    }

    public ProductListResDto(String id, String title, int price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }
}
