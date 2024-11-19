package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/product/save")
    public Mono<Product> save(@RequestPart("product") ProductSaveReqDto productSaveReqDto,
                              @RequestPart(value = "image") FilePart image, // 상품 이미지는 반드시 필요 (required = true (기본값))
                              @AuthenticationPrincipal CustomUserDetail userDetail) {
        return productService.saveProduct(productSaveReqDto, image, userDetail.getUser());
    }

    @PutMapping("/product/update")
    public Mono<Void> update(@RequestPart("product") ProductUpdateReqDto productUpdateReqDto,
                             @RequestPart(value = "image", required = false) FilePart image) {
        return productService.updateProduct(productUpdateReqDto, image);
    }

    @DeleteMapping("/product/delete")
    public Mono<Void> delete(@RequestBody ProductDeleteReqDto productDeleteReqDto) {
        return productService.deleteProduct(productDeleteReqDto);
    }
}
