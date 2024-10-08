package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
}
