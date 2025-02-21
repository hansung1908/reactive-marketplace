package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.ProductStatus;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductApiControllerTest {
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductApiController productApiController;

    @Test
    void save_ShouldReturnCreatedStatus() {
        // Given
        ProductSaveReqDto saveReqDto = new ProductSaveReqDto(
                "testProduct",
                "test description",
                1000,
                ImageSource.PRODUCT
        );
        FilePart mockImage = mock(FilePart.class);
        Authentication mockAuth = mock(Authentication.class);
        when(productService.saveProduct(any(ProductSaveReqDto.class), any(FilePart.class), any(Authentication.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(productApiController.save(saveReqDto, mockImage, mockAuth))
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.CREATED &&
                                response.getBody().equals("Product saved successfully")
                )
                .verifyComplete();
    }

    @Test
    void update_ShouldReturnOkStatus() {
        // Given
        ProductUpdateReqDto updateReqDto = new ProductUpdateReqDto(
                "testId",
                "test description",
                1000,
                ProductStatus.ON_SALE,
                ImageSource.PRODUCT
        );
        FilePart mockImage = mock(FilePart.class);
        Authentication mockAuth = mock(Authentication.class);
        when(productService.updateProduct(any(ProductUpdateReqDto.class), any(FilePart.class), any(Authentication.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(productApiController.update(updateReqDto, mockImage, mockAuth))
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.OK &&
                                response.getBody().equals("Product updated successfully")
                )
                .verifyComplete();
    }

    @Test
    void delete_ShouldReturnOkStatus() {
        // Given
        ProductDeleteReqDto deleteReqDto = new ProductDeleteReqDto(
                "testId"
        );
        Authentication mockAuth = mock(Authentication.class);
        when(productService.deleteProduct(any(ProductDeleteReqDto.class), any(Authentication.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(productApiController.delete(deleteReqDto, mockAuth))
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.OK &&
                                response.getBody().equals("Product deleted successfully")
                )
                .verifyComplete();
    }
}

