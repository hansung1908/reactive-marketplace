package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> findAll() {
        return productRepository.findAll().subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Product> save(ProductSaveReqDto productSaveReqDto, User user) {
        Product product = new Product.Builder()
                .title(productSaveReqDto.getTitle())
                .description(productSaveReqDto.getDescription())
                .price(productSaveReqDto.getPrice())
                .userId(user.getId())
                .build();

        return productRepository.save(product);
    }
}
