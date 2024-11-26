package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductDetailResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    // 상품 저장
    Mono<Product> saveProduct(ProductSaveReqDto productSaveReqDto, FilePart image, User user);

    // 상품 상세 조회
    Mono<ProductDetailResDto> findProductDetail(String productId);

    // 상품 목록 조회
    Flux<ProductListResDto> findProductList();

    // 내 상품 목록 조회
    Flux<MyProductListResDto> findMyProductList(String userId);

    // 상품 ID로 조회
    Mono<Product> findProductById(String productId);

    // 상품 수정
    Mono<Void> updateProduct(ProductUpdateReqDto productUpdateReqDto, FilePart image);

    // 상품 삭제
    Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto);
}
