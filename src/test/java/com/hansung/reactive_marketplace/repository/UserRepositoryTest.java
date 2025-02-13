package com.hansung.reactive_marketplace.repository;

import com.hansung.reactive_marketplace.config.MongoConfig;
import com.hansung.reactive_marketplace.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

@DataMongoTest
@Import(MongoConfig.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User.Builder()
                .username("testUser")
                .password("password")
                .nickname("nickname")
                .email("test@test.com")
                .build();

        userRepository.save(testUser)
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findByUsernameTest() {
        userRepository.findByUsername("testUser")
                .as(StepVerifier::create)
                .expectNextMatches(foundUser ->
                        foundUser.getUsername().equals("testUser") &&
                                foundUser.getNickname().equals("nickname") &&
                                foundUser.getEmail().equals("test@test.com"))
                .verifyComplete();
    }

    @Test
    void existsByUsernameTest() {
        userRepository.existsByUsername("testUser")
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void updateUserTest() {
        userRepository.updateUser(
                        testUser.getId(),
                        "newNickname",
                        "newPassword",
                        "new@test.com"
                )
                .then(userRepository.findById(testUser.getId()))
                .as(StepVerifier::create)
                .expectNextMatches(updatedUser ->
                        updatedUser.getNickname().equals("newNickname") &&
                                updatedUser.getPassword().equals("newPassword") &&
                                updatedUser.getEmail().equals("new@test.com"))
                .verifyComplete();
    }

    @Test
    void findByUsername_WhenUserNotExists() {
        // when & then
        userRepository.findByUsername("nonexistent")
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void existsByUsername_WhenUserNotExists() {
        // when & then
        userRepository.existsByUsername("nonexistent")
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }
}

