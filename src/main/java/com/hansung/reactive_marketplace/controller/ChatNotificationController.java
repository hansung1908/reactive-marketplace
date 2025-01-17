package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.service.messaging.RedisSubscriber;
import com.hansung.reactive_marketplace.util.AuthUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatNotificationController {
    // 다중 구독자에게 메시지를 브로드캐스트하기 위한 Sink
    private final RedisSubscriber redisSubscriber;

    public ChatNotificationController(RedisSubscriber redisSubscriber) {
        this.redisSubscriber = redisSubscriber;
    }

    // 클라이언트에게 실시간으로 채팅 메시지를 스트리밍
    @GetMapping(value = "/chat/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Chat>> streamChatNotifications(Authentication authentication) {
        return redisSubscriber.subscribeToTopic(AuthUtils.getAuthenticationUser(authentication).getId());
    }
}

