package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Image;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ImageRepository extends ReactiveMongoRepository<Image, String> {
}
