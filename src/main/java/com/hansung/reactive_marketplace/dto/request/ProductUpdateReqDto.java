package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ProductStatus;
import lombok.Getter;

@Getter
public class ProductUpdateReqDto {

    private String id;

    private String description;

    private int price;

    private ProductStatus status;

    private String imagePath;

    protected ProductUpdateReqDto() {
    }

    public ProductUpdateReqDto(String id, String description, int price, ProductStatus status, String imagePath) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.status = status;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "ProductUpdateReqDto{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
