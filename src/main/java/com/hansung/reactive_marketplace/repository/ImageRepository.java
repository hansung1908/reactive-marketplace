package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Image;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ImageRepository extends ReactiveMongoRepository<Image, String> {

    Mono<Image> findByProductId(String productId);

    Mono<Image> findByUserId(String userId);

    Mono<Void> deleteByProductId(String productId);
}
