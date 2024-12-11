package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.ChatService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat/open/{productId}")
    public Mono<Rendering> openChat(@PathVariable("productId") String productId, Authentication authentication) {
        return chatService.openChatByBuyerId(productId, authentication)
                .map(chatRoom -> Rendering.view("chat/chatForm")
                        .modelAttribute("chatRoom", chatRoom)
                        .build());
    }

    @GetMapping("/chat/chatRoomList")
    public Mono<Rendering> findChatRoomList(Authentication authentication) {
        return Mono.just(Rendering.view("chat/chatRoomForm")
                .modelAttribute("chatRooms", chatService.findChatRoomList(authentication))
                .build());
    }
}
