package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.service.ChatService;
import com.hansung.reactive_marketplace.service.messaging.RedisPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ChatApiController {

    private final ChatService chatService;

    private final RedisPublisher redisPublisher;

    public ChatApiController(ChatService chatService, RedisPublisher redisPublisher) {
        this.chatService = chatService;
        this.redisPublisher = redisPublisher;
    }

    @GetMapping(value = "/chat/{roomId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> findMsg(@PathVariable("roomId") String roomId) {
        return chatService.findMsgByRoomId(roomId);
    }

    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> saveMsg(@RequestBody ChatSaveReqDto chatSaveReqDto) {
        return chatService.saveMsg(chatSaveReqDto)
                .flatMap(savedMessage -> {
                    // Redis를 통해 메시지 발행
                    return redisPublisher.publish("chat-messages", savedMessage)
                            .then(Mono.just(savedMessage));
                })
                .map(savedMessage -> ResponseEntity.status(HttpStatus.CREATED)
                        .body("Chat message saved and published successfully"));
    }
}
