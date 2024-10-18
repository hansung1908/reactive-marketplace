package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Flux<Product> findAll(Sort sort);
}
