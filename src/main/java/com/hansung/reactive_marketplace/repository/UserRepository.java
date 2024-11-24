package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'nickname' : ?1, 'password' : ?2, 'email' : ?3 }}")
    Mono<Void> updateUser(String id, String nickname, String password, String email);
}
