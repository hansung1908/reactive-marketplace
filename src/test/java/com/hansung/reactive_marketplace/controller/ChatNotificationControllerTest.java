package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.redis.RedisSubscriber;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatNotificationControllerTest {
    @Mock
    private RedisSubscriber redisSubscriber;

    @InjectMocks
    private ChatNotificationController chatNotificationController;

    @Test
    void streamChatNotifications_ShouldReturnNotificationFlux() {
        // Given
        Authentication authentication = mock(Authentication.class);
        User user = new User.Builder()
                .username("testUser")
                .nickname("nickname")
                .password("testPassword")
                .email("test@email.com")
                .build();
        ReflectionTestUtils.setField(user, "id", "testId");

        CustomUserDetail userDetail = mock(CustomUserDetail.class);
        when(authentication.getPrincipal()).thenReturn(userDetail);
        when(userDetail.getUser()).thenReturn(user);
        ServerSentEvent<String> event1 = ServerSentEvent.builder("message1").build();
        ServerSentEvent<String> event2 = ServerSentEvent.builder("message2").build();

        when(redisSubscriber.subscribeToTopic("testId"))
                .thenReturn(Flux.just(event1, event2));

        // When & Then
        StepVerifier.create(chatNotificationController.streamChatNotifications(authentication))
                .expectNext(event1)
                .expectNext(event2)
                .verifyComplete();
    }
}

