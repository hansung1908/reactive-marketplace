package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/product/save")
    public Mono<Product> save(@RequestBody ProductSaveReqDto productSaveReqDto,
                              @AuthenticationPrincipal CustomUserDetail userDetail) {
        return productService.save(productSaveReqDto, userDetail.getUser());
    }
}
