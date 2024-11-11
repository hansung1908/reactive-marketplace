package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;
import lombok.Getter;

@Getter
public class ProductSaveReqDto {

    private String title;

    private String description;

    private int price;

    private ImageSource imageSource;

    protected ProductSaveReqDto() {
    }

    public ProductSaveReqDto(String title, String description, int price, ImageSource imageSource) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageSource = imageSource;
    }

    @Override
    public String toString() {
        return "ProductSaveReqDto{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageSource=" + imageSource +
                '}';
    }
}
