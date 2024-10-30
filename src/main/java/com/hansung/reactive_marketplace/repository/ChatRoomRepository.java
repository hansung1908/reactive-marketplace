package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.domain.ChatRoom;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRoomRepository extends ReactiveMongoRepository<ChatRoom, String> {

    @Query(value = "{ 'productId' : ?0, 'seller' : ?1 }")
    Mono<ChatRoom> findChatRoom(String productId, String seller);

    @Query("{ $or: [ { seller: ?0 }, { buyer: ?0 } ] }")
    Flux<ChatRoom> findChatRoomListBySellerOrBuyer(String nickname);
}
