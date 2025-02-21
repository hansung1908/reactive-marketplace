package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.ProductStatus;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductDetailResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductUpdateResDto;
import com.hansung.reactive_marketplace.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void saveForm_ShouldReturnCorrectViewName() {
        String viewName = productController.saveForm();
        assertThat(viewName).isEqualTo("product/saveForm");
    }

    @Test
    void productDetail_ShouldReturnDetailView() {
        // Given
        String productId = "1";
        Authentication authentication = mock(Authentication.class);
        ProductDetailResDto productDetail = new ProductDetailResDto(
                "testId",
                "testProduct",
                1000,
                "test description",
                "nickname",
                "path/to/image",
                "testSellerId",
                "testBuyerId"
        ); // 예시 DTO
        when(productService.findProductDetail(productId, authentication))
                .thenReturn(Mono.just(productDetail));

        // When & Then
        StepVerifier.create(productController.ProductDetail(productId, authentication))
                .expectNextMatches(rendering ->
                        rendering.view().equals("product/detailForm") &&
                                rendering.modelAttributes().containsKey("product") &&
                                rendering.modelAttributes().get("product").equals(productDetail)
                )
                .verifyComplete();
    }

    @Test
    void productList_ShouldReturnIndexView() {
        // Given
        Flux<ProductListResDto> productList = Flux.just(new ProductListResDto(
                "testId",
                "testProduct",
                1000,
                "path/to/image"
        ));
        when(productService.findProductList())
                .thenReturn(productList);

        // When & Then
        StepVerifier.create(productController.ProductList())
                .expectNextMatches(rendering ->
                        rendering.view().equals("index") &&
                                rendering.modelAttributes().containsKey("productList")
                )
                .verifyComplete();
    }

    @Test
    void myProductList_ShouldReturnMyListView() {
        // Given
        Authentication mockAuth = mock(Authentication.class);
        Flux<MyProductListResDto> myProducts = Flux.just(new MyProductListResDto(
                "testId",
                "testProduct",
                "test description",
                1000,
                ProductStatus.ON_SALE,
                "2024-02-21 23:27",
                "path/to/image"
        ));
        when(productService.findMyProductList(mockAuth))
                .thenReturn(myProducts);

        // When & Then
        StepVerifier.create(productController.MyProductList(mockAuth))
                .expectNextMatches(rendering ->
                        rendering.view().equals("product/myListForm") &&
                                rendering.modelAttributes().containsKey("myProductList")
                )
                .verifyComplete();
    }

    @Test
    void updateForm_ShouldReturnUpdateView() {
        // Given
        String productId = "1";
        ProductUpdateResDto product = new ProductUpdateResDto(
                "testId",
                "testProduct",
                1000,
                "test description",
                "nickname",
                "path/to/image"
        ); // 예시 DTO
        when(productService.findProductForUpdateForm(productId))
                .thenReturn(Mono.just(product));

        // When & Then
        StepVerifier.create(productController.updateForm(productId))
                .expectNextMatches(rendering ->
                        rendering.view().equals("product/updateForm") &&
                                rendering.modelAttributes().containsKey("product") &&
                                rendering.modelAttributes().get("product").equals(product)
                )
                .verifyComplete();
    }
}


