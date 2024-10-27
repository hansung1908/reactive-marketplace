package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat/productId/{productId}/seller/{seller}/buyer/{buyer}")
    public Mono<Rendering> openChat(@PathVariable("productId") String productId,
                                    @PathVariable("seller") String seller,
                                    @PathVariable("buyer") String buyer) {
        return chatService.openChat(productId, seller, buyer)
                .map(chatRoom -> Rendering.view("chat/chatForm")
                        .modelAttribute("chatRoom", chatRoom)
                        .build());
    }
}
