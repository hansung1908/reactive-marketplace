package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    @Query(value = "{}", fields = "{ '_id' : 1, 'title' : 1, 'price' : 1 }")
    Flux<ProductListResDto> findProductList(Sort sort);

    @Query(value = "{ 'userId' : ?0 }", fields = "{ '_id' : 1, 'title' : 1, 'description' : 1, 'price' : 1, 'status' : 1, 'createdAt' : 1 }")
    Flux<MyProductListResDto> findMyProductList(String userId, Sort sort);
}
