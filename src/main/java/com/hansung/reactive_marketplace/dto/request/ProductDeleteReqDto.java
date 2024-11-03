package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class ProductDeleteReqDto {

    private String id;

    protected ProductDeleteReqDto() {
    }

    public ProductDeleteReqDto(String id) {
        this.id = id;
    }
}
