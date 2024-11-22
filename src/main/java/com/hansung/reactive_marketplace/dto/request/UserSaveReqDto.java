package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;

public record UserSaveReqDto(
        String username,
        String nickname,
        String password,
        String email,
        ImageSource imageSource) {}
