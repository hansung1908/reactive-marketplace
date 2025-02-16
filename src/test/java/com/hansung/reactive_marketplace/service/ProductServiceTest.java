package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.*;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductDetailResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private RedisCacheManager redisCacheManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductSaveReqDto testProductSaveReqDto;
    private ProductUpdateReqDto testProductUpdateReqDto;
    private ProductDeleteReqDto testProductDeleteReqDto;
    private Authentication authentication;
    private FilePart testFilePart;
    private User testUser;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testProduct = new Product.Builder()
                .title("Test Product")
                .description("Test Description")
                .price(10000)
                .userId("testUserId")
                .build();

        ReflectionTestUtils.setField(testProduct, "id", "testProductId");

        testProductSaveReqDto = new ProductSaveReqDto(
                "Test Product",
                "Test Description",
                10000,
                ImageSource.PRODUCT
        );

        testProductUpdateReqDto = new ProductUpdateReqDto(
                "testProductId",
                "Updated Description",
                20000,
                ProductStatus.SOLD_OUT,
                ImageSource.PRODUCT
        );

        testProductDeleteReqDto = new ProductDeleteReqDto("testProductId");

        testUser = new User.Builder()
                .username("testUser")
                .nickname("testNickname")
                .build();

        ReflectionTestUtils.setField(testUser, "id", "testId");

        authentication = mock(Authentication.class);
        testFilePart = mock(FilePart.class);
    }

    private void authenticationSetUp() {
        CustomUserDetail userDetail = mock(CustomUserDetail.class);
        when(authentication.getPrincipal()).thenReturn(userDetail);
        when(userDetail.getUser()).thenReturn(testUser);
    }

    @Test
    void givenProductSaveReqDto_whenSaveProduct_thenSuccess() {
        authenticationSetUp();

        when(productRepository.save(any()))
                .thenReturn(Mono.just(testProduct));
        when(imageService.uploadImage(any(), any(), any()))
                .thenReturn(Mono.empty());
        when(redisCacheManager.deleteList(any()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(productService.saveProduct(testProductSaveReqDto, testFilePart, authentication))
                .expectNextMatches(product ->
                        product.getTitle().equals("Test Product") &&
                                product.getDescription().equals("Test Description") &&
                                product.getPrice() == 10000)
                .verifyComplete();
    }

    @Test
    void givenProductSaveReqDto_whenSaveProduct_thenSaveFailed() {
        authenticationSetUp();

        when(productRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(productService.saveProduct(testProductSaveReqDto, testFilePart, authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_SAVE_FAILED))
                .verify();
    }

    @Test
    void givenProductId_whenFindProductDetail_thenSuccess() {
        String cacheKey = "product:testProductId";
        ProductDetailResDto expectedDto = new ProductDetailResDto(
                testProduct.getId(),
                testProduct.getTitle(),
                testProduct.getPrice(),
                testProduct.getDescription(),
                testUser.getNickname(),
                "/test/image.jpg",
                testProduct.getUserId(),
                testUser.getId()
        );

        when(redisCacheManager.getOrFetch(
                eq(cacheKey),
                eq(ProductDetailResDto.class),
                any(Mono.class),
                eq(Duration.ofHours(1))))
                .thenReturn(Mono.just(expectedDto));
        when(productRepository.findById("testProductId"))
                .thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.findProductDetail("testProductId", authentication))
                .expectNextMatches(dto ->
                        dto.id().equals("testProductId") &&
                                dto.title().equals("Test Product") &&
                                dto.price() == 10000 &&
                                dto.description().equals("Test Description") &&
                                dto.nickname().equals("testNickname"))
                .verifyComplete();
    }

    @Test
    void givenProductId_whenFindProductDetail_thenProductNotFound() {
        String cacheKey = "product:nonexistentId";

        when(redisCacheManager.getOrFetch(
                eq(cacheKey),
                eq(ProductDetailResDto.class),
                any(Mono.class),
                eq(Duration.ofHours(1))))
                .thenReturn(Mono.empty());
        when(productRepository.findById("nonexistentId"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.findProductDetail("nonexistentId", authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_NOT_FOUND))
                .verify();
    }

    @Test
    void givenProductUpdateReqDto_whenUpdateProduct_thenSuccess() {
        authenticationSetUp();

        when(productRepository.findById("testProductId")).thenReturn(Mono.just(testProduct));
        when(productRepository.updateProduct(any(), any(), anyInt(), any())).thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue(anyString())).thenReturn(Mono.empty());
        when(redisCacheManager.deleteList(anyString())).thenReturn(Mono.empty());
        when(imageService.deleteProductImageById(anyString())).thenReturn(Mono.empty());
        when(imageService.uploadImage(any(), anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateProduct(testProductUpdateReqDto, testFilePart, authentication))
                .verifyComplete();
    }

    @Test
    void givenProductId_whenDeleteProduct_thenSuccess() {
        authenticationSetUp();

        when(productRepository.findById("testProductId"))
                .thenReturn(Mono.just(testProduct));
        when(productRepository.deleteById((String) any()))
                .thenReturn(Mono.empty());
        when(imageService.deleteProductImageById(any()))
                .thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue(any()))
                .thenReturn(Mono.just(true));
        when(redisCacheManager.deleteList(any()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(productService.deleteProduct(testProductDeleteReqDto, authentication))
                .verifyComplete();
    }

    @Test
    void givenProductId_whenFindProductForUpdateForm_thenSuccess() {
        when(productRepository.findById("testProductId"))
                .thenReturn(Mono.just(testProduct));
        when(imageService.findProductImageById("testProductId"))
                .thenReturn(Mono.just(new Image.Builder()
                        .imagePath("/test/image.jpg")
                        .build()));
        when(userService.findUserById("testUserId"))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(productService.findProductForUpdateForm("testProductId"))
                .expectNextMatches(dto ->
                        dto.id().equals("testProductId") &&
                                dto.title().equals("Test Product") &&
                                dto.price() == 10000 &&
                                dto.description().equals("Test Description") &&
                                dto.nickname().equals("testNickname") &&
                                dto.imagePath().equals("/test/image.jpg"))
                .verifyComplete();
    }

    @Test
    void givenProductId_whenFindProductForUpdateForm_thenProductNotFound() {
        when(productRepository.findById("nonexistentId"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.findProductForUpdateForm("nonexistentId"))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_NOT_FOUND))
                .verify();
    }

    @Test
    void givenNoParameters_whenFindProductList_thenSuccess() {
        Product product1 = new Product.Builder()
                .title("Product 1")
                .description("Description 1")
                .price(10000)
                .userId("user1")
                .build();
        ReflectionTestUtils.setField(product1, "id", "product1");

        Product product2 = new Product.Builder()
                .title("Product 2")
                .description("Description 2")
                .price(20000)
                .userId("user2")
                .build();
        ReflectionTestUtils.setField(product2, "id", "product2");

        when(productRepository.findProductList(any(Sort.class)))
                .thenReturn(Flux.just(product1, product2));
        when(imageService.findProductImageById("product1"))
                .thenReturn(Mono.just(new Image.Builder()
                        .thumbnailPath("/thumbnail1.jpg")
                        .build()));
        when(imageService.findProductImageById("product2"))
                .thenReturn(Mono.just(new Image.Builder()
                        .thumbnailPath("/thumbnail2.jpg")
                        .build()));

        StepVerifier.create(productService.findProductList())
                .expectNextMatches(dto ->
                        dto.id().equals("product1") &&
                                dto.title().equals("Product 1") &&
                                dto.price() == 10000 &&
                                dto.thumbnailPath().equals("/thumbnail1.jpg"))
                .expectNextMatches(dto ->
                        dto.id().equals("product2") &&
                                dto.title().equals("Product 2") &&
                                dto.price() == 20000 &&
                                dto.thumbnailPath().equals("/thumbnail2.jpg"))
                .verifyComplete();
    }

    @Test
    void givenAuthentication_whenFindMyProductList_thenSuccess() {
        authenticationSetUp();

        Product product1 = new Product.Builder()
                .title("My Product 1")
                .description("Description 1")
                .price(10000)
                .userId(testUser.getId())
                .build();
        ReflectionTestUtils.setField(product1, "id", "product1");
        ReflectionTestUtils.setField(product1, "createdAt", LocalDateTime.now());

        Product product2 = new Product.Builder()
                .title("My Product 2")
                .description("Description 2")
                .price(20000)
                .userId(testUser.getId())
                .build();
        ReflectionTestUtils.setField(product2, "id", "product2");
        ReflectionTestUtils.setField(product2, "createdAt", LocalDateTime.now());

        when(productRepository.findMyProductList(eq(testUser.getId()), any(Sort.class)))
                .thenReturn(Flux.just(product1, product2));
        when(imageService.findProductImageById("product1"))
                .thenReturn(Mono.just(new Image.Builder()
                        .thumbnailPath("/thumbnail1.jpg")
                        .build()));
        when(imageService.findProductImageById("product2"))
                .thenReturn(Mono.just(new Image.Builder()
                        .thumbnailPath("/thumbnail2.jpg")
                        .build()));
        when(redisCacheManager.getOrFetchList(
                eq("myProductList:" + testUser.getId()),
                eq(MyProductListResDto.class),
                any(Flux.class),
                any(Duration.class)))
                .thenAnswer(invocation -> {
                    Flux<MyProductListResDto> fallback = invocation.getArgument(2);
                    return fallback;
                });

        StepVerifier.create(productService.findMyProductList(authentication))
                .expectNextMatches(dto ->
                        dto.id().equals("product1") &&
                                dto.title().equals("My Product 1") &&
                                dto.description().equals("Description 1") &&
                                dto.price() == 10000 &&
                                dto.thumbnailPath().equals("/thumbnail1.jpg"))
                .expectNextMatches(dto ->
                        dto.id().equals("product2") &&
                                dto.title().equals("My Product 2") &&
                                dto.description().equals("Description 2") &&
                                dto.price() == 20000 &&
                                dto.thumbnailPath().equals("/thumbnail2.jpg"))
                .verifyComplete();
    }

    @Test
    void givenProductId_whenFindProductById_thenSuccess() {
        when(productRepository.findById("testProductId"))
                .thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.findProductById("testProductId"))
                .expectNextMatches(product ->
                        product.getId().equals("testProductId") &&
                                product.getTitle().equals("Test Product") &&
                                product.getDescription().equals("Test Description") &&
                                product.getPrice() == 10000)
                .verifyComplete();
    }

    @Test
    void givenProductId_whenFindProductById_thenProductNotFound() {
        when(productRepository.findById("nonexistentId"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.findProductById("nonexistentId"))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_NOT_FOUND))
                .verify();
    }
}
