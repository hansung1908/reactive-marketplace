package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = "/chat/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> openChat(@PathVariable String productId) {
        return chatService.findMsgByProductId(productId);
    }
}
