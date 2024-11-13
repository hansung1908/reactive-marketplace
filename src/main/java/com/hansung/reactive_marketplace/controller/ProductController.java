package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.dto.response.ProductDetailResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import com.hansung.reactive_marketplace.service.ImageService;
import com.hansung.reactive_marketplace.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.List;

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
        Mono<Image> image = imageService.findProductImageById(id);
        Mono<ProductDetailResDto> product = productService.findProductDetail(id);

        return Mono.zip(image, product)
                .map(tuple -> {
                    Image productImage = tuple.getT1();
                    ProductDetailResDto productDetail = tuple.getT2();

                    return Rendering.view("product/detailForm")
                            .modelAttribute("productImage", productImage)
                            .modelAttribute("productDetail", productDetail)
                            .build();
                });
    }

    @GetMapping("/")
    public Mono<Rendering> ProductList() {
        return productService.findProductList()
                .collectList()
                .map(productList -> Rendering.view("index")
                        .modelAttribute("productList", productList)
                        .build());
    }

    @GetMapping("/product/my/{userId}")
    public Mono<Rendering> MyProductList(@PathVariable("userId") String userId) {
        return productService.findMyProductList(userId)
                .collectList()
                .map(myProductList -> Rendering.view("product/myProductListForm")
                        .modelAttribute("myProductList", myProductList)
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