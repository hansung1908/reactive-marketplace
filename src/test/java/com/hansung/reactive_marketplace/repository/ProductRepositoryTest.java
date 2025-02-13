package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.config.MongoConfig;
import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.domain.ProductStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import reactor.test.StepVerifier;

import java.util.Arrays;

@DataMongoTest
@Import(MongoConfig.class)
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Product otherUserProduct;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product.Builder()
                .userId("testUser")
                .title("Test Product 1")
                .description("Test Description 1")
                .price(10000)
                .build();

        testProduct2 = new Product.Builder()
                .userId("testUser")
                .title("Test Product 2")
                .description("Test Description 2")
                .price(20000)
                .build();

        otherUserProduct = new Product.Builder()
                .userId("otherUser")
                .title("Other Product")
                .description("Other Description")
                .price(30000)
                .build();

        productRepository.saveAll(Arrays.asList(testProduct1, testProduct2, otherUserProduct))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findProductListTest() {
        productRepository.findProductList(Sort.by(Sort.Direction.DESC, "price"))
                .as(StepVerifier::create)
                .expectNextMatches(product ->
                        product.getPrice() == 30000 &&
                                product.getTitle().equals("Other Product"))
                .expectNextMatches(product ->
                        product.getPrice() == 20000 &&
                                product.getTitle().equals("Test Product 2"))
                .expectNextMatches(product ->
                        product.getPrice() == 10000 &&
                                product.getTitle().equals("Test Product 1"))
                .verifyComplete();
    }

    @Test
    void findMyProductListTest() {
        productRepository.findMyProductList("testUser",
                        Sort.by(Sort.Direction.ASC, "price"))
                .as(StepVerifier::create)
                .expectNextMatches(product ->
                        product.getPrice() == 10000 &&
                                product.getUserId().equals("testUser"))
                .expectNextMatches(product ->
                        product.getPrice() == 20000 &&
                                product.getUserId().equals("testUser"))
                .verifyComplete();
    }

    @Test
    void updateProductTest() {
        productRepository.updateProduct(
                        testProduct1.getId(),
                        "Updated Description",
                        15000,
                        ProductStatus.SOLD_OUT
                )
                .then(productRepository.findById(testProduct1.getId()))
                .as(StepVerifier::create)
                .expectNextMatches(updatedProduct ->
                        updatedProduct.getDescription().equals("Updated Description") &&
                                updatedProduct.getPrice() == 15000 &&
                                updatedProduct.getStatus() == ProductStatus.SOLD_OUT)
                .verifyComplete();
    }

    @Test
    void findMyProductList_NoProducts() {
        productRepository.findMyProductList("nonexistentUser",
                        Sort.by(Sort.Direction.DESC, "price"))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}

