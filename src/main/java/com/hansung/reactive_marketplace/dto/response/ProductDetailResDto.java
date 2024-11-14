package com.hansung.reactive_marketplace.dto.response;

import lombok.Getter;

@Getter
public class ProductDetailResDto {

    private String id;

    private String title;

    private int price;

    private String description;

    private String userId;

    private String nickname;

    private String imagePath;

    protected ProductDetailResDto() {
    }

    public ProductDetailResDto(String id, String title, int price, String description, String userId, String nickname, String imagePath) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.userId = userId;
        this.nickname = nickname;
        this.imagePath = imagePath;
    }
}
