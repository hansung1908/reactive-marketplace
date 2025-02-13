package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.config.MongoConfig;
import com.hansung.reactive_marketplace.domain.Chat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.time.Duration;

@DataMongoTest
@Import(MongoConfig.class)
class ChatRepositoryTest {
    @Autowired
    private ChatRepository chatRepository;

    private Chat testChat1;
    private Chat testChat2;

    @BeforeEach
    void setUp() {
        testChat1 = new Chat.Builder()
                .roomId("room1")
                .senderId("user1")
                .receiverId("user2")
                .msg("Hello")
                .build();

        testChat2 = new Chat.Builder()
                .roomId("room1")
                .senderId("user2")
                .receiverId("user1")
                .msg("Hi there")
                .build();

        chatRepository.save(testChat1)
                .delayElement(Duration.ofSeconds(1))
                .thenMany(chatRepository.save(testChat2))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        chatRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findMsgByRoomIdTest() {
        chatRepository.findMsgByRoomId("room1")
                .as(StepVerifier::create)
                .expectNextMatches(chat ->
                        chat.getRoomId().equals("room1") &&
                                chat.getSenderId().equals("user1") &&
                                chat.getReceiverId().equals("user2") &&
                                chat.getMsg().equals("Hello"))
                .expectNextMatches(chat ->
                        chat.getRoomId().equals("room1") &&
                                chat.getSenderId().equals("user2") &&
                                chat.getReceiverId().equals("user1") &&
                                chat.getMsg().equals("Hi there"))
                .thenCancel()
                .verify();
    }

    @Test
    void findRecentChatTest() {
        chatRepository.findRecentChat("room1")
                .as(StepVerifier::create)
                .expectNextMatches(chat ->
                        chat.getRoomId().equals("room1") &&
                                chat.getSenderId().equals("user2") &&
                                chat.getReceiverId().equals("user1") &&
                                chat.getMsg().equals("Hi there"))
                .verifyComplete();
    }

    @Test
    void findMsgByRoomId_WhenRoomEmpty() {
        chatRepository.findMsgByRoomId("nonexistent")
                .as(StepVerifier::create)
                .thenCancel()
                .verify();
    }

    @Test
    void findRecentChat_WhenRoomEmpty() {
        chatRepository.findRecentChat("nonexistent")
                .as(StepVerifier::create)
                .verifyComplete();
    }
}


