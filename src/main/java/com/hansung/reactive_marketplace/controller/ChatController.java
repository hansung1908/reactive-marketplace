package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.ChatService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat/open")
    public Mono<Rendering> openChat(@RequestParam("productId") String productId,
                                    @RequestParam("sellerId") String sellerId,
                                    @RequestParam("buyerId") String buyerId) {
        return chatService.openChat(productId, sellerId, buyerId)
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
