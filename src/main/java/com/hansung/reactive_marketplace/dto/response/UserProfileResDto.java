package com.hansung.reactive_marketplace.dto.response;

public record UserProfileResDto(
   String username,
   String nickname,
   String email,
   String imagePath
) {}
