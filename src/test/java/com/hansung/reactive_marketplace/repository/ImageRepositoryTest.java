package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.config.MongoConfig;
import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.Arrays;

@DataMongoTest
@Import(MongoConfig.class)
class ImageRepositoryTest {
    @Autowired
    private ImageRepository imageRepository;

    private Image testImage1;
    private Image testImage2;

    @BeforeEach
    void setUp() {
        testImage1 = new Image.Builder()
                .imageSource(ImageSource.PRODUCT)
                .userId("user1")
                .productId("product1")
                .imageName("test_image1.jpg")
                .imageType("image/jpeg")
                .imageSize(1024L)
                .imagePath("/images/products/test_image1.jpg")
                .thumbnailPath("/images/products/thumbnails/test_image1.jpg")
                .build();

        testImage2 = new Image.Builder()
                .imageSource(ImageSource.PROFILE)
                .userId("user2")
                .imageName("test_image2.jpg")
                .imageType("image/jpeg")
                .imageSize(2048L)
                .imagePath("/images/profiles/test_image2.jpg")
                .thumbnailPath("/images/profiles/thumbnails/test_image2.jpg")
                .build();

        imageRepository.deleteAll()
                .thenMany(imageRepository.saveAll(Arrays.asList(testImage1, testImage2)))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        imageRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findByProductIdTest() {
        imageRepository.findByProductId("product1")
                .as(StepVerifier::create)
                .expectNextMatches(image ->
                        image.getProductId().equals("product1") &&
                                image.getImageSource() == ImageSource.PRODUCT &&
                                image.getImageName().equals("test_image1.jpg") &&
                                image.getImagePath().equals("/images/products/test_image1.jpg"))
                .verifyComplete();
    }

    @Test
    void findByUserIdTest() {
        imageRepository.findByUserId("user2")
                .as(StepVerifier::create)
                .expectNextMatches(image ->
                        image.getUserId().equals("user2") &&
                                image.getImageSource() == ImageSource.PROFILE &&
                                image.getImageName().equals("test_image2.jpg") &&
                                image.getImagePath().equals("/images/profiles/test_image2.jpg"))
                .verifyComplete();
    }

    @Test
    void deleteByProductIdTest() {
        imageRepository.deleteByProductId("product1")
                .then(imageRepository.findByProductId("product1"))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deleteByUserIdTest() {
        imageRepository.deleteByUserId("user2")
                .then(imageRepository.findByUserId("user2"))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findByProductId_WhenNotExists() {
        imageRepository.findByProductId("nonexistent")
                .as(StepVerifier::create)
                .verifyComplete();
    }
}

