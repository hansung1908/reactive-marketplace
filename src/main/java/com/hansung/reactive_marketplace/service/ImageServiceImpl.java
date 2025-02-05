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
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ImageServiceImpl implements ImageService {

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
        return Mono.justOrEmpty(image)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(img -> createImageData(img, id, imageSource))
                .flatMap(imageData -> saveImageFiles(image, imageData, imageSource))
                .flatMap(imageData -> imageRepository.save(imageData))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_UPLOAD_FAILED));
    }

    private Mono<Image> createImageData(FilePart image, String id, ImageSource imageSource) {
        String imageName = ImageUtils.generateUniqueImageName(image.filename());

        return Mono.just(new Image.Builder()
                .imageName(image.filename())
                .imageType(String.valueOf(image.headers().getContentType()))
                .imageSize(image.headers().getContentLength())
                .imageSource(imageSource)
                .userId(imageSource == ImageSource.PROFILE ? id : null)
                .productId(imageSource == ImageSource.PRODUCT ? id : null)
                .imagePath(ImageUtils.generateImagePath(
                        imageSource == ImageSource.PROFILE ? profileOriginalPath : productOriginalPath,
                        imageName
                ))
                .thumbnailPath(ImageUtils.generateImagePath(
                        imageSource == ImageSource.PROFILE ? profileThumbnailPath : productThumbnailPath,
                        "resized_" + imageName
                ))
                .build());
    }

    private Mono<Image> saveImageFiles(FilePart image, Image imageData, ImageSource imageSource) {
        File originalFile = new File(imageData.getImagePath());
        File resizedFile = new File(imageData.getThumbnailPath());

        return image.transferTo(originalFile)
                .then(Mono.fromCallable(() -> {
                    Thumbnails.of(originalFile)
                            .forceSize(
                                    imageSource == ImageSource.PROFILE ? 45 : 300,
                                    imageSource == ImageSource.PROFILE ? 45 : 350
                            )
                            .outputQuality(0.8)
                            .toFile(resizedFile);
                    return imageData;
                }));
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
                .flatMap(image -> deleteImageFiles(image))
                .then(imageRepository.deleteByProductId(productId))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
    }

    public Mono<Void> deleteProfileImageById(String userId) {
        return imageRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(image -> deleteImageFiles(image))
                .then(imageRepository.deleteByUserId(userId))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
    }

    private Mono<Image> deleteImageFiles(Image image) {
        return Mono.fromCallable(() -> {
                    Files.delete(Paths.get(image.getImagePath()));
                    Files.delete(Paths.get(image.getThumbnailPath()));
                    return image;
                });
    }
}
