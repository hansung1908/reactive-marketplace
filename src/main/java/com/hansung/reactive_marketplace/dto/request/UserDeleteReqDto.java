package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class UserDeleteReqDto {

    private String id;

    protected UserDeleteReqDto() {
    }

    public UserDeleteReqDto(String id) {
        this.id = id;
    }
}
