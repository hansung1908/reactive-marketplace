package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.*;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
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
import org.springframework.transaction.reactive.TransactionalOperator;
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

    @Mock
    private TransactionalOperator transactionalOperator;

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

        testFilePart = mock(FilePart.class);
    }

    private void authenticationSetUp() {
        authentication = mock(Authentication.class);
        CustomUserDetail userDetail = mock(CustomUserDetail.class);
        when(authentication.getPrincipal()).thenReturn(userDetail);
        when(userDetail.getUser()).thenReturn(testUser);
    }

    private void transactionalSetUp() {
        // 트랜잭션 호출시 첫번째 인자를 그대로 반환하도록 지정
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSaveProduct_WhenGivenValidRequest_ThenProductIsSavedSuccessfully() {
        authenticationSetUp();
        transactionalSetUp();

        when(productRepository.save(any()))
                .thenReturn(Mono.just(testProduct));
        when(imageService.uploadImage(any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.saveProduct(testProductSaveReqDto, testFilePart, authentication))
                .expectNextMatches(product ->
                        product.getTitle().equals("Test Product") &&
                                product.getDescription().equals("Test Description") &&
                                product.getPrice() == 10000)
                .verifyComplete();
    }

    @Test
    void testSaveProduct_WhenDatabaseErrorOccurs_ThenThrowApiException() {
        authenticationSetUp();
        transactionalSetUp();

        when(productRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(productService.saveProduct(testProductSaveReqDto, testFilePart, authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.INTERNAL_SERVER_ERROR))
                .verify();
    }

    @Test
    void testFindProductDetail_WhenProductExists_ThenReturnProductDetail() {
        authenticationSetUp();

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
                eq("product:testProductId:user:testId"),
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
    void testFindProductDetail_WhenProductDoesNotExist_ThenThrowApiException() {
        authenticationSetUp();

        String cacheKey = "product:nonexistentId";

        when(redisCacheManager.getOrFetch(
                eq("product:nonexistentId:user:testId"),
                eq(ProductDetailResDto.class),
                any(Mono.class),
                eq(Duration.ofHours(1))))
                .thenReturn(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)));
        when(productRepository.findById("nonexistentId"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.findProductDetail("nonexistentId", authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_NOT_FOUND))
                .verify();
    }

    @Test
    void testUpdateProduct_WhenGivenValidRequest_ThenProductIsUpdatedSuccessfully() {
        transactionalSetUp();
        when(productRepository.findById("testProductId")).thenReturn(Mono.just(testProduct));
        when(productRepository.updateProduct(any(), any(), anyInt(), any())).thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue(anyString())).thenReturn(Mono.empty());
        when(imageService.deleteProductImageById(anyString())).thenReturn(Mono.empty());
        when(imageService.uploadImage(any(), anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateProduct(testProductUpdateReqDto, testFilePart, authentication))
                .verifyComplete();
    }

    @Test
    void testDeleteProduct_WhenProductExists_ThenProductIsDeletedSuccessfully() {
        transactionalSetUp();
        when(productRepository.findById("testProductId"))
                .thenReturn(Mono.just(testProduct));
        when(productRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());
        when(imageService.deleteProductImageById(any()))
                .thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue(any()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(productService.deleteProduct(testProductDeleteReqDto, authentication))
                .verifyComplete();
    }

    @Test
    void testFindProductForUpdateForm_WhenProductExists_ThenReturnProductDetails() {
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
    void testFindProductForUpdateForm_WhenProductDoesNotExist_ThenThrowApiException() {
        when(productRepository.findById("nonexistentId"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.findProductForUpdateForm("nonexistentId"))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_NOT_FOUND))
                .verify();
    }

    @Test
    void testFindProductList_WhenNoParametersProvided_ThenReturnProductList() {
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
    void testFindMyProductList_WhenUserAuthenticated_ThenReturnUserProducts() {
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
    void testFindProductById_WhenProductExists_ThenReturnProduct() {
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
    void testFindProductById_WhenProductDoesNotExist_ThenThrowApiException() {
        when(productRepository.findById("nonexistentId"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.findProductById("nonexistentId"))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.PRODUCT_NOT_FOUND))
                .verify();
    }
}
