package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ImageService {

    // 이미지 업로드 메소드
    Mono<Image> uploadImage(FilePart image, String id, ImageSource imageSource);

    // 상품 이미지 ID로 찾기
    Mono<Image> findProductImageById(String productId);

    // 사용자 프로필 이미지 ID로 찾기
    Mono<Image> findProfileImageById(String userId);

    // 상품 이미지 삭제
    Mono<Void> deleteProductImageById(String productId);

    // 사용자 프로필 이미지 삭제
    Mono<Void> deleteProfileImageById(String userId);
}
