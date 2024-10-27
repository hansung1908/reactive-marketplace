package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Chat;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {

    @Tailable
    @Query(value = "{ 'roomId': ?0 }")
    Flux<Chat> findMsgByRoomId(String roomId);
}
