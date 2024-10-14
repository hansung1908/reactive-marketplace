package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/product/saveForm")
    public String saveForm() {
        return "product/saveForm";
    }

    @GetMapping("/product/{id}")
    public Mono<String> getProductDetails(@PathVariable String id, Model model) {
        return productService.findById(id)
                .doOnNext(product -> model.addAttribute(product))
                .then(Mono.just("product/detailForm"));
    }
}
