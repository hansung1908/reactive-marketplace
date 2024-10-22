package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public Mono<Rendering> index() {
        return productService.findProductList()
                .collectList()
                .map(products -> Rendering.view("index")
                        .modelAttribute("products", products)
                        .build());
    }

    @GetMapping("/product/saveForm")
    public String saveForm() {
        return "product/saveForm";
    }

    @GetMapping("/product/{id}")
    public Mono<Rendering> ProductDetails(@PathVariable("id") String id) {
        return productService.findById(id)
                .map(product -> Rendering.view("product/detailForm")
                        .modelAttribute("product", product)
                        .build());
    }

    @GetMapping("/product/my/{userId}")
    public Mono<Rendering> MyProductList(@PathVariable("userId") String userId) {
        return productService.findMyProductList(userId)
                .collectList()
                .map(products -> Rendering.view("product/my/productListForm")
                        .modelAttribute("products", products)
                        .build());
    }
}