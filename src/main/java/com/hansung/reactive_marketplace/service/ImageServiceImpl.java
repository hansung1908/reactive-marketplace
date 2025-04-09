package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.ImageRepository;
import com.hansung.reactive_marketplace.util.ImageUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    private final RedisCacheManager redisCacheManager;

    private final TransactionalOperator transactionalOperator;

    @Value("${image.profile.originalPath}")
    private String profileOriginalPath;

    @Value("${image.profile.thumbnailPath}")
    private String profileThumbnailPath;

    @Value("${image.product.originalPath}")
    private String productOriginalPath;

    @Value("${image.product.thumbnailPath}")
    private String productThumbnailPath;

    @Value("${image.os.basePath}")
    private String osBasePath;

    public ImageServiceImpl(ImageRepository imageRepository, RedisCacheManager redisCacheManager, TransactionalOperator transactionalOperator) {
        this.imageRepository = imageRepository;
        this.redisCacheManager = redisCacheManager;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<Image> uploadImage(FilePart image, String id, ImageSource imageSource) {
        return Mono.justOrEmpty(image)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(img -> createImageData(img, id, imageSource))
                .flatMap(imageData -> imageRepository.save(imageData))
                .as(transactionalOperator::transactional)
                .flatMap(imageData -> saveImageFile(image, imageData, imageSource))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_UPLOAD_FAILED));
    }

    private Mono<Image> createImageData(FilePart image, String id, ImageSource imageSource) {
        String imageName = ImageUtils.generateUniqueImageName(image.filename());

        return getFileSize(image)
                .flatMap(fileSize -> Mono.just(new Image.Builder()
                        .imageName(image.filename())
                        .imageType(String.valueOf(image.headers().getContentType()))
                        .imageSize(fileSize)
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
                        .build()));
    }

    private Mono<Long> getFileSize(FilePart filePart) {
        return DataBufferUtils.join(filePart.content()) // 버퍼를 하나로 결합
                .map(dataBuffer -> {
                    long size = dataBuffer.readableByteCount();  // 결합된 전체 크기
                    DataBufferUtils.release(dataBuffer);
                    return size;
                });
    }

    private Mono<Image> saveImageFile(FilePart image, Image imageData, ImageSource imageSource) {
        File originalFile = new File(osBasePath + imageData.getImagePath());
        File resizedFile = new File(osBasePath + imageData.getThumbnailPath());

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

    public Mono<Image> findProductImageByIdWithCache(String productId) {
        return redisCacheManager.getOrFetch(
                "productImage:" + productId,
                Image.class,
                findProductImageById(productId),
                Duration.ofHours(1)
        );
    }

    public Mono<Image> findProfileImageById(String userId) {
        return imageRepository.findByUserId(userId)
                .switchIfEmpty(Mono.fromCallable(() -> new Image.Builder() // 이미지가 없으면 기본 이미지로 대체
                        .imagePath("/img/profile.png")
                        .thumbnailPath("/img/profile.png")
                        .build())
                );
    }

    public Mono<Image> findProfileImageByIdWithCache(String userId) {
        return redisCacheManager.getOrFetch(
                "userImage:" + userId,
                Image.class,
                findProfileImageById(userId),
                Duration.ofHours(1)
        );
    }

    public Mono<Void> deleteProductImageById(String productId) {
        return imageRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(image -> imageRepository.deleteByProductId(productId).thenReturn(image))
                .as(transactionalOperator::transactional)
                .flatMap(image -> deleteImageFiles(image))
                .then(Mono.defer(() -> redisCacheManager.deleteValue("productImage:" + productId)))
                .then()
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
    }

    public Mono<Void> deleteProfileImageById(String userId) {
        return imageRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)))
                .flatMap(image -> imageRepository.deleteByUserId(userId).thenReturn(image))
                .as(transactionalOperator::transactional)
                .flatMap(image -> deleteImageFiles(image))
                .then(Mono.defer(() -> redisCacheManager.deleteValue("userImage:" + userId)))
                .then()
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.IMAGE_DELETE_FAILED));
    }

    private Mono<Image> deleteImageFiles(Image image) {
        return Mono.fromCallable(() -> {
            Files.delete(Paths.get(osBasePath + image.getImagePath()));
            Files.delete(Paths.get(osBasePath + image.getThumbnailPath()));
            return image;
        });
    }
}
