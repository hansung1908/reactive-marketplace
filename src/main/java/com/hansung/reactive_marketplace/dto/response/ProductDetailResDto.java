package com.hansung.reactive_marketplace.dto.response;

import lombok.Getter;

@Getter
public class ProductDetailResDto {

    private String title;

    private int price;

    private String description;

    private String nickname;

    protected ProductDetailResDto() {
    }

    public ProductDetailResDto(String title, int price, String description, String nickname) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.nickname = nickname;
    }
}
