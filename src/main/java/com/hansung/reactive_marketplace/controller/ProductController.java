package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.ImageService;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

    private final ProductService productService;

    private final ImageService imageService;

    public ProductController(ProductService productService, ImageService imageService) {
        this.productService = productService;
        this.imageService = imageService;
    }

    @GetMapping("/product/saveForm")
    public String saveForm() {
        return "product/saveForm";
    }

    @GetMapping("/product/detail/{id}")
    public Mono<Rendering> ProductDetail(@PathVariable("id") String id) {
        return productService.findProductDetail(id)
                .map(product -> Rendering.view("product/detailForm")
                        .modelAttribute("product", product)
                        .build());
    }

    @GetMapping("/")
    public Mono<Rendering> ProductList() {
        return Mono.just(Rendering.view("index")
                .modelAttribute("productList", productService.findProductList())
                .build());
    }

    @GetMapping("/product/my/{userId}")
    public Mono<Rendering> MyProductList(@PathVariable("userId") String userId) {
        return Mono.just(Rendering.view("product/myProductListForm")
                .modelAttribute("myProductList", productService.findMyProductList(userId))
                .build());
    }

    @GetMapping("/product/updateForm/{id}")
    public Mono<Rendering> updateForm(@PathVariable("id") String id) {
        return productService.findProductDetail(id)
                .map(product -> Rendering.view("product/updateForm")
                        .modelAttribute("product", product)
                        .build());
    }
}