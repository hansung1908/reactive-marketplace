package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private RedisCacheManager redisCacheManager;

    @Mock
    private FilePart filePart;

    private ImageServiceImpl imageService;

    private String productId;
    private String userId;
    private String fileName;
    private String uniqueFileName;
    private long fileSize;
    private Image expectedImage;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl(imageRepository, redisCacheManager);
        
        // @Value로 초기화 하는 부분
        ReflectionTestUtils.setField(imageService, "profileOriginalPath", "test/profile/original");
        ReflectionTestUtils.setField(imageService, "profileThumbnailPath", "test/profile/thumbnail");
        ReflectionTestUtils.setField(imageService, "productOriginalPath", "test/product/original");
        ReflectionTestUtils.setField(imageService, "productThumbnailPath", "test/product/thumbnail");

        productId = "testProduct";
        userId = "testUser";
        fileName = "test.jpg";
        uniqueFileName = "test-uuid_" + fileName;
        fileSize = 1024L;

        expectedImage = new Image.Builder()
            .imageName(fileName)
            .imageType(MediaType.IMAGE_JPEG_VALUE)
            .imageSize(fileSize)
            .imageSource(ImageSource.PROFILE)
            .userId(userId)
            .imagePath("test/profile/original/" + uniqueFileName)
            .thumbnailPath("test/profile/thumbnail/resized_" + uniqueFileName)
            .build();
    }

    private void givenProductImage() {
        Image image = new Image.Builder()
                .imageName("test.jpg")
                .imageType("image/jpeg")
                .imageSize(1000L)
                .imageSource(ImageSource.PRODUCT)
                .productId(productId)
                .imagePath("/path/to/image.jpg")
                .thumbnailPath("/path/to/thumbnail.jpg")
                .build();

        when(imageRepository.findByProductId(productId))
                .thenReturn(Mono.just(image));
        when(redisCacheManager.getOrFetch(
                eq("productImage:" + productId),
                eq(Image.class),
                any(Mono.class),
                any(Duration.class)
        )).thenReturn(Mono.just(image));
    }

    private void givenProfileImage() {
        Image image = new Image.Builder()
                .imageName("profile.jpg")
                .imageType("image/jpeg")
                .imageSize(1000L)
                .imageSource(ImageSource.PROFILE)
                .userId(userId)
                .imagePath("/path/to/profile.jpg")
                .thumbnailPath("/path/to/profile_thumbnail.jpg")
                .build();

        when(imageRepository.findByUserId(userId))
                .thenReturn(Mono.just(image));
        when(redisCacheManager.getOrFetch(
                eq("userImage:" + userId),
                eq(Image.class),
                any(Mono.class),
                any(Duration.class)
        )).thenReturn(Mono.just(image));
    }

    @Test
    void givenProductImage_whenFindProductImageByIdWithCache_thenSuccess() {
        // given
        givenProductImage();

        // when & then
        StepVerifier.create(imageService.findProductImageByIdWithCache(productId))
                .expectNextMatches(image -> image.getImageName().equals("test.jpg"))
                .verifyComplete();
    }

    @Test
    void givenProfileImage_whenFindProfileImageByIdWithCache_thenSuccess() {
        // given
        givenProfileImage();

        // when & then
        StepVerifier.create(imageService.findProfileImageByIdWithCache(userId))
                .expectNextMatches(image -> image.getImageName().equals("profile.jpg"))
                .verifyComplete();
    }
}
