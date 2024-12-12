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

    @GetMapping("/chat/chatRoom/{productId}/{buyerId}")
    public Mono<Rendering> openChatBySeller(@PathVariable("productId") String productId,
                                            @PathVariable("buyerId") String buyerId,
                                            Authentication authentication) {
        return chatService.openChatBySeller(productId, buyerId, authentication)
                .map(chatRoom -> Rendering.view("chat/chatRoomForm")
                        .modelAttribute("chatRoom", chatRoom)
                        .build());
    }

    @GetMapping("/chat/chatRoom/{productId}")
    public Mono<Rendering> openChatByBuyer(@PathVariable("productId") String productId, Authentication authentication) {
        return chatService.openChatByBuyer(productId, authentication)
                .map(chatRoom -> Rendering.view("chat/chatRoomForm")
                        .modelAttribute("chatRoom", chatRoom)
                        .build());
    }

    @GetMapping("/chat/chatRoomList")
    public Mono<Rendering> findChatRoomList(Authentication authentication) {
        return Mono.just(Rendering.view("chat/chatRoomListForm")
                .modelAttribute("sellerChatRoomList", chatService.findChatRoomListBySeller(authentication))
                .modelAttribute("buyerChatRoomList", chatService.findChatRoomListByBuyer(authentication))
                .build());
    }
}
