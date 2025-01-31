package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.repository.ImageRepository;
import com.hansung.reactive_marketplace.util.ImageUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ImageServiceImpl implements ImageService{

    private final ImageRepository imageRepository;

    @Value("${image.profile.originalPath}")
    private String profileOriginalPath;

    @Value("${image.profile.thumbnailPath}")
    private String profileThumbnailPath;

    @Value("${image.product.originalPath}")
    private String productOriginalPath;

    @Value("${image.product.thumbnailPath}")
    private String productThumbnailPath;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Mono<Image> uploadImage(FilePart image, String id, ImageSource imageSource) {
        if (image == null) {
            return Mono.error(new ApiException(ExceptionMessage.IMAGE_UPLOAD_FAILED));
        }

        String imageName = ImageUtils.generateUniqueImageName(image.filename());

        Image imageData = new Image.Builder()
                .imageName(image.filename())
                .imageType(String.valueOf(image.headers().getContentType()))
                .imageSize(image.headers().getContentLength())
                .build();

        switch (imageSource) { // 이미지 타입에 따라 분류
            case PROFILE -> {
                imageData = imageData.toBuilder()
                        .imageSource(ImageSource.PROFILE)
                        .userId(id)
                        .imagePath(ImageUtils.generateImagePath(profileOriginalPath, imageName))
                        .thumbnailPath(ImageUtils.generateImagePath(profileThumbnailPath, "resized_" + imageName))
                        .build();

                File originalProfileImage = new File(profileOriginalPath, imageName); // 원본 이미지 파일 경로
                File resizedProfileImage = new File(profileThumbnailPath, "resized_" + imageName); // 리사이즈된 이미지 파일 경로

                try {
                    image.transferTo(originalProfileImage).subscribe(); // 원본 이미지 업로드

                    Thumbnails.of(originalProfileImage) // 해당 경로로부터 이미지 가져옴
                            .forceSize(45, 45) // 이미지 크기를 리사이즈
                            .outputQuality(0.8) // 품질을 80%로 설정 (0.0 ~ 1.0 범위)
                            .toFile(resizedProfileImage); // 리사이즈된 이미지 업로드
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case PRODUCT -> {
                imageData = imageData.toBuilder()
                        .imageSource(ImageSource.PRODUCT)
                        .productId(id)
                        .imagePath(ImageUtils.generateImagePath(productOriginalPath, imageName))
                        .thumbnailPath(ImageUtils.generateImagePath(productThumbnailPath, "resized_" + imageName))
                        .build();

                File originalProductImage = new File(productOriginalPath, imageName);
                File resizedProductImage = new File(productThumbnailPath, "resized_" + imageName);

                try {
                    image.transferTo(originalProductImage).subscribe();

                    Thumbnails.of(originalProductImage)
                            .forceSize(300, 350)
                            .outputQuality(0.8)
                            .toFile(resizedProductImage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return imageRepository.save(imageData)
                .onErrorMap(e -> new ApiException(ExceptionMessage.IMAGE_UPLOAD_FAILED));
    }

    public Mono<Image> findProductImageById(String productId) {
        return imageRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)));
    }

    public Mono<Image> findProfileImageById(String userId) {
        return imageRepository.findByUserId(userId)
                .switchIfEmpty(Mono.fromCallable(() -> new Image.Builder() // 이미지가 없으면 기본 이미지로 대체
                                    .imagePath("/img/profile.png")
                                    .thumbnailPath("/img/profile.png")
                                    .build())
                            );
    }

    public Mono<Void> deleteProductImageById(String productId) {
        return imageRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(image -> {
                    try {
                        Files.delete(Paths.get(image.getImagePath()));
                        Files.delete(Paths.get(image.getThumbnailPath()));
                        return Mono.just(image);
                    } catch (IOException e) {
                        return Mono.error(new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
                    }
                })
                .then(imageRepository.deleteByProductId(productId))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
    }

    public Mono<Void> deleteProfileImageById(String userId) {
        return imageRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(image -> {
                    try {
                        Files.delete(Paths.get(image.getImagePath()));
                        Files.delete(Paths.get(image.getThumbnailPath()));
                        return Mono.just(image);
                    } catch (IOException e) {
                        return Mono.error(new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
                    }
                })
                .then(imageRepository.deleteByUserId(userId))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
    }
}
