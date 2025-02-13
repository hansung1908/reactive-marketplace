package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.config.MongoConfig;
import com.hansung.reactive_marketplace.domain.ChatRoom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.Arrays;

@DataMongoTest
@Import(MongoConfig.class)
class ChatRoomRepositoryTest {
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private ChatRoom testChatRoom1;
    private ChatRoom testChatRoom2;

    @BeforeEach
    void setUp() {
        testChatRoom1 = new ChatRoom.Builder()
                .productId("product1")
                .sellerId("seller1")
                .buyerId("buyer1")
                .build();

        testChatRoom2 = new ChatRoom.Builder()
                .productId("product2")
                .sellerId("seller1")
                .buyerId("buyer2")
                .build();

        chatRoomRepository.saveAll(Arrays.asList(testChatRoom1, testChatRoom2))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }
    @Test
    void findChatRoomTest() {
        chatRoomRepository.findChatRoom("product1", "buyer1")
                .as(StepVerifier::create)
                .expectNextMatches(chatRoom ->
                        chatRoom.getProductId().equals("product1") &&
                                chatRoom.getBuyerId().equals("buyer1") &&
                                chatRoom.getSellerId().equals("seller1"))
                .verifyComplete();
    }

    @Test
    void findChatRoomListBySellerTest() {
        chatRoomRepository.findChatRoomListBySeller("seller1")
                .as(StepVerifier::create)
                .expectNextMatches(chatRoom ->
                        chatRoom.getProductId().equals("product1") &&
                                chatRoom.getSellerId().equals("seller1"))
                .expectNextMatches(chatRoom ->
                        chatRoom.getProductId().equals("product2") &&
                                chatRoom.getSellerId().equals("seller1"))
                .verifyComplete();
    }

    @Test
    void findChatRoomListByBuyerTest() {
        chatRoomRepository.findChatRoomListByBuyer("buyer1")
                .as(StepVerifier::create)
                .expectNextMatches(chatRoom ->
                        chatRoom.getProductId().equals("product1") &&
                                chatRoom.getBuyerId().equals("buyer1"))
                .verifyComplete();
    }

    @Test
    void findChatRoom_WhenNotExists() {
        chatRoomRepository.findChatRoom("nonexistent", "buyer1")
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findChatRoomListBySeller_WhenEmpty() {
        chatRoomRepository.findChatRoomListBySeller("nonexistent")
                .as(StepVerifier::create)
                .verifyComplete();
    }
}

