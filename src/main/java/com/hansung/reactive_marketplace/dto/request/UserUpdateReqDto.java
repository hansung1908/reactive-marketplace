package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;

public record UserUpdateReqDto(
        String id,
        String nickname,
        String password,
        String email,
        ImageSource imageSource) {}
