package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.ProductStatus;
import lombok.Getter;

@Getter
public class ProductUpdateReqDto {

    private String id;

    private String description;

    private int price;

    private ProductStatus status;

    private ImageSource imageSource;

    protected ProductUpdateReqDto() {
    }

    public ProductUpdateReqDto(String id, String description, int price, ProductStatus status, ImageSource imageSource) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.status = status;
        this.imageSource = imageSource;
    }

    @Override
    public String toString() {
        return "ProductUpdateReqDto{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", imageSource=" + imageSource +
                '}';
    }
}
