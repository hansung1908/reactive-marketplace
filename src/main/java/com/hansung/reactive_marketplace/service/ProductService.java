package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> findAll() {
        return productRepository.findAll().subscribeOn(Schedulers.boundedElastic());
    }
}
