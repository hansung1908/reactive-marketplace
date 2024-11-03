package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.domain.ProductStatus;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    @Query(value = "{}", fields = "{ '_id' : 1, 'title' : 1, 'price' : 1 }")
    Flux<ProductListResDto> findProductList(Sort sort);

    @Query(value = "{ 'userId' : ?0 }", fields = "{ '_id' : 1, 'title' : 1, 'description' : 1, 'price' : 1, 'status' : 1, 'createdAt' : 1 }")
    Flux<MyProductListResDto> findMyProductList(String userId, Sort sort);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'description' : ?1, 'price' : ?2, 'status' : ?3 }}")
    Mono<Void> updateProduct(String id, String description, int price, ProductStatus status);
}
