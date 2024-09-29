package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class ProductSaveReqDto {

    private String title;

    private String description;

    private int price;

    protected ProductSaveReqDto() {
    }

    public ProductSaveReqDto(String title, String description, int price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductSaveReqDto{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
