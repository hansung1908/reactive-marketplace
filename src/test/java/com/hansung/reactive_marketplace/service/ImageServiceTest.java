package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.ImageRepository;
import com.hansung.reactive_marketplace.util.ImageUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private RedisCacheManager redisCacheManager;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private ImageServiceImpl imageService;

    private String productId;
    private String userId;
    private String fileName;
    private String uniqueFileName;
    private long fileSize;
    private Image userImage;
    private Image productImage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "profileOriginalPath", "test/profile/original");
        ReflectionTestUtils.setField(imageService, "profileThumbnailPath", "test/profile/thumbnail");
        ReflectionTestUtils.setField(imageService, "productOriginalPath", "test/product/original");
        ReflectionTestUtils.setField(imageService, "productThumbnailPath", "test/product/thumbnail");

        productId = "testProduct";
        userId = "testUser";
        fileName = "test.jpg";
        uniqueFileName = "test-uuid_" + fileName;
        fileSize = 1024L;

        userImage = new Image.Builder()
                .imageName(fileName)
                .imageType(MediaType.IMAGE_JPEG_VALUE)
                .imageSize(fileSize)
                .imageSource(ImageSource.PROFILE)
                .userId(userId)
                .imagePath("test/profile/original/" + uniqueFileName)
                .thumbnailPath("test/profile/thumbnail/resized_" + uniqueFileName)
                .build();

        productImage = new Image.Builder()
                .imageName(fileName)
                .imageType(MediaType.IMAGE_JPEG_VALUE)
                .imageSize(fileSize)
                .imageSource(ImageSource.PRODUCT)
                .productId(productId)
                .imagePath("test/product/original/" + uniqueFileName)
                .thumbnailPath("test/product/thumbnail/resized_" + uniqueFileName)
                .build();
    }

    @Test
    void testUploadImage_WhenValidFileProvided_ThenImageUploadedSuccessfully() {
        try (MockedStatic<ImageUtils> imageUtils = Mockito.mockStatic(ImageUtils.class);
             MockedStatic<Thumbnails> thumbnails = Mockito.mockStatic(Thumbnails.class)) {

            imageUtils.when(() -> ImageUtils.generateUniqueImageName(anyString())).thenReturn(uniqueFileName);
            imageUtils.when(() -> ImageUtils.generateImagePath(anyString(), anyString())).thenCallRealMethod();

            Thumbnails.Builder<File> builder = mock(Thumbnails.Builder.class);
            when(builder.forceSize(any(Integer.class), any(Integer.class))).thenReturn(builder);
            when(builder.outputQuality(any(Double.class))).thenReturn(builder);
            try {
                doNothing().when(builder).toFile(any(File.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thumbnails.when(() -> Thumbnails.of(any(File.class))).thenReturn(builder);

            when(filePart.filename()).thenReturn(fileName);
            when(filePart.headers()).thenReturn(new org.springframework.http.HttpHeaders() {{
                setContentType(MediaType.IMAGE_JPEG);
                setContentLength(fileSize);
            }});
            when(filePart.transferTo(any(File.class))).thenReturn(Mono.empty());

            when(imageRepository.save(any(Image.class))).thenReturn(Mono.just(userImage));

            imageService.uploadImage(filePart, userId, ImageSource.PROFILE)
                    .as(StepVerifier::create)
                    .expectNextMatches(image ->
                            image.getImageName().equals(fileName) &&
                                    image.getImageType().equals(MediaType.IMAGE_JPEG_VALUE) &&
                                    image.getImageSize() == fileSize &&
                                    image.getImageSource() == ImageSource.PROFILE &&
                                    image.getUserId().equals(userId) &&
                                    image.getImagePath().equals("test/profile/original/" + uniqueFileName) &&
                                    image.getThumbnailPath().equals("test/profile/thumbnail/resized_" + uniqueFileName)
                    )
                    .verifyComplete();
        }
    }

    @Test
    void testUploadImage_WhenValidProductFileProvided_ThenProductImageUploadedSuccessfully() {
        try (MockedStatic<ImageUtils> imageUtils = Mockito.mockStatic(ImageUtils.class);
             MockedStatic<Thumbnails> thumbnails = Mockito.mockStatic(Thumbnails.class)) {

            Image productImage = new Image.Builder()
                    .imageName(fileName)
                    .imageType(MediaType.IMAGE_JPEG_VALUE)
                    .imageSize(fileSize)
                    .imageSource(ImageSource.PRODUCT)
                    .productId(productId)
                    .imagePath("test/product/original/" + uniqueFileName)
                    .thumbnailPath("test/product/thumbnail/resized_" + uniqueFileName)
                    .build();

            imageUtils.when(() -> ImageUtils.generateUniqueImageName(anyString())).thenReturn(uniqueFileName);
            imageUtils.when(() -> ImageUtils.generateImagePath(anyString(), anyString())).thenCallRealMethod();

            Thumbnails.Builder<File> builder = mock(Thumbnails.Builder.class);
            when(builder.forceSize(any(Integer.class), any(Integer.class))).thenReturn(builder);
            when(builder.outputQuality(any(Double.class))).thenReturn(builder);
            try {
                doNothing().when(builder).toFile(any(File.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thumbnails.when(() -> Thumbnails.of(any(File.class))).thenReturn(builder);

            when(filePart.filename()).thenReturn(fileName);
            when(filePart.headers()).thenReturn(new org.springframework.http.HttpHeaders() {{
                setContentType(MediaType.IMAGE_JPEG);
                setContentLength(fileSize);
            }});
            when(filePart.transferTo(any(File.class))).thenReturn(Mono.empty());

            when(imageRepository.save(any(Image.class))).thenReturn(Mono.just(productImage));

            imageService.uploadImage(filePart, productId, ImageSource.PRODUCT)
                    .as(StepVerifier::create)
                    .expectNextMatches(image ->
                            image.getImageName().equals(fileName) &&
                                    image.getImageType().equals(MediaType.IMAGE_JPEG_VALUE) &&
                                    image.getImageSize() == fileSize &&
                                    image.getImageSource() == ImageSource.PRODUCT &&
                                    image.getProductId().equals(productId) &&
                                    image.getImagePath().equals("test/product/original/" + uniqueFileName) &&
                                    image.getThumbnailPath().equals("test/product/thumbnail/resized_" + uniqueFileName)
                    )
                    .verifyComplete();
        }
    }

    @Test
    void testFindProductImageById_WhenProductExists_ThenReturnImage() {
        when(imageRepository.findByProductId(productId)).thenReturn(Mono.just(userImage));

        imageService.findProductImageById(productId)
                .as(StepVerifier::create)
                .expectNext(userImage)
                .verifyComplete();
    }

    @Test
    void testFindProductImageById_WhenProductDoesNotExist_ThenThrowApiException() {
        String productId = "nonExistentProduct";
        when(imageRepository.findByProductId(productId)).thenReturn(Mono.empty());

        imageService.findProductImageById(productId)
                .as(StepVerifier::create)
                .expectError(ApiException.class)
                .verify();
    }

    @Test
    void testFindProductImageByIdWithCache_WhenProductExistsInCache_ThenReturnImage() {
        when(redisCacheManager.getOrFetch(
                anyString(),
                eq(Image.class),
                any(),
                any(Duration.class)
        )).thenAnswer(invocation -> {
            Mono<Image> fetchFunction = invocation.getArgument(2);
            return fetchFunction;
        });

        when(imageRepository.findByProductId(productId)).thenReturn(Mono.just(userImage));

        imageService.findProductImageByIdWithCache(productId)
                .as(StepVerifier::create)
                .expectNext(userImage)
                .verifyComplete();
    }

    @Test
    void testFindProfileImageById_WhenUserExists_ThenReturnImage() {
        when(imageRepository.findByUserId(userId)).thenReturn(Mono.just(userImage));

        imageService.findProfileImageById(userId)
                .as(StepVerifier::create)
                .expectNext(userImage)
                .verifyComplete();
    }

    @Test
    void testFindProfileImageById_WhenUserDoesNotExist_ThenReturnDefaultImage() {
        when(imageRepository.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(imageService.findProfileImageById(userId))
                .expectNextMatches(image ->
                        "/img/profile.png".equals(image.getImagePath()) &&
                                "/img/profile.png".equals(image.getThumbnailPath()) &&
                                image.getImageSize() == 0 &&
                                image.getId() == null &&
                                image.getImageSource() == null &&
                                image.getUserId() == null &&
                                image.getProductId() == null &&
                                image.getImageName() == null &&
                                image.getImageType() == null &&
                                image.getCreatedAt() == null
                )
                .verifyComplete();
    }

    @Test
    void testFindProfileImageByIdWithCache_WhenUserExistsInCache_ThenReturnImage() {
        when(redisCacheManager.getOrFetch(
                anyString(),
                eq(Image.class),
                any(),
                any(Duration.class)
        )).thenAnswer(invocation -> {
            Mono<Image> fetchFunction = invocation.getArgument(2);
            return fetchFunction;
        });

        when(imageRepository.findByUserId(userId)).thenReturn(Mono.just(userImage));

        imageService.findProfileImageByIdWithCache(userId)
                .as(StepVerifier::create)
                .expectNext(userImage)
                .verifyComplete();
    }

    @Test
    void testDeleteProductImageById_WhenProductExists_ThenDeleteImageSuccessfully() {
        when(imageRepository.findByProductId(productId)).thenReturn(Mono.just(productImage));
        when(imageRepository.deleteByProductId(productId)).thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue(anyString())).thenReturn(Mono.empty());

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.delete(any(Path.class))).thenReturn(true);

            StepVerifier.create(imageService.deleteProductImageById(productId))
                    .verifyComplete();
        }
    }

    @Test
    void testDeleteProfileImageById_WhenUserExists_ThenDeleteImageSuccessfully() {
        when(imageRepository.findByUserId(userId)).thenReturn(Mono.just(userImage));
        when(imageRepository.deleteByUserId(userId)).thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue(anyString())).thenReturn(Mono.empty());

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.delete(any(Path.class))).thenReturn(true);

            StepVerifier.create(imageService.deleteProfileImageById(userId))
                    .verifyComplete();
        }
    }

    @Test
    void testDeleteProductImageById_WhenProductDoesNotExist_ThenThrowApiException() {
        when(imageRepository.findByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(imageService.deleteProductImageById(productId))
                .expectError(ApiException.class)
                .verify();
    }

    @Test
    void testDeleteProfileImageById_WhenUserDoesNotExist_ThenThrowApiException() {
        when(imageRepository.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(imageService.deleteProfileImageById(userId))
                .expectError(ApiException.class)
                .verify();
    }
}
