package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
