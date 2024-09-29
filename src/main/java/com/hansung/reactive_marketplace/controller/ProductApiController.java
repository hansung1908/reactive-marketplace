package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public Flux<Product> index() {
        return productService.findAll();
    }

    @PostMapping("/product/save")
    public Mono<Product> save(@RequestBody ProductSaveReqDto productSaveReqDto) {
        return productService.save(productSaveReqDto);
    }
}
