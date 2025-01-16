package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Chat;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
public class ChatNotificationController {
    // 다중 구독자에게 메시지를 브로드캐스트하기 위한 Sink
    private final Sinks.Many<Chat> chatSink = Sinks.many().multicast().onBackpressureBuffer(1000);

    // 클라이언트에게 실시간으로 채팅 메시지를 스트리밍
    @GetMapping(value = "/chat/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Chat>> streamChatNotifications() {
        return chatSink.asFlux()
                .map(message -> ServerSentEvent.<Chat>builder()
                        .event("chat-message")
                        .data(message)
                        .build())
                .doOnCancel(() -> System.out.println("Client disconnected from chat stream"));
    }

    // Redis에서 수신한 메시지를 Sink에 전달하는 메소드
    public void sendNotification(Chat chat) {
        chatSink.tryEmitNext(chat);
    }
}

