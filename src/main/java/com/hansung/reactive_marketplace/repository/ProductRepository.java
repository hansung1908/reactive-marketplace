package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.domain.ProductStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    @Query(value = "{ 'status' : 'ON_SALE' }", fields = "{ '_id' : 1, 'title' : 1, 'price' : 1 }")
    Flux<Product> findProductList(Sort sort);

    @Query(value = "{ 'userId' : ?0 }")
    Flux<Product> findMyProductList(String userId, Sort sort);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'description' : ?1, 'price' : ?2, 'status' : ?3 }}")
    Mono<Void> updateProduct(String id, String description, int price, ProductStatus status);
}
