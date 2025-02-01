package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/product/save")
    public Mono<ResponseEntity<String>> save(@RequestPart("product") ProductSaveReqDto productSaveReqDto,
                                             @RequestPart(value = "image") FilePart image,
                                             Authentication authentication) {
        return productService.saveProduct(productSaveReqDto, image, authentication)
                .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                        .body("Product saved successfully")));
    }

    @PutMapping("/product/update")
    public Mono<ResponseEntity<String>> update(@RequestPart("product") ProductUpdateReqDto productUpdateReqDto,
                                               @RequestPart(value = "image", required = false) FilePart image,
                                               Authentication authentication) {
        return productService.updateProduct(productUpdateReqDto, image, authentication)
                .then(Mono.just(ResponseEntity.ok("Product updated successfully")));
    }

    @DeleteMapping("/product/delete")
    public Mono<ResponseEntity<String>> delete(@RequestBody ProductDeleteReqDto productDeleteReqDto, Authentication authentication) {
        return productService.deleteProduct(productDeleteReqDto, authentication)
                .then(Mono.just(ResponseEntity.ok("Product deleted successfully")));
    }
}
