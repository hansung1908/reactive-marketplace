package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.Chat;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {

    @Tailable
    @Query(value = "{ 'roomId': ?0 }")
    Flux<Chat> findMsgByRoomId(String roomId);

    @Aggregation(pipeline = {
            "{ '$match': { 'roomId': ?0 } }",
            "{ '$sort': { 'createdAt': -1 } }",
            "{ '$limit': 1 }"
    })
    Mono<Chat> findRecentChat(String roomId);
}
