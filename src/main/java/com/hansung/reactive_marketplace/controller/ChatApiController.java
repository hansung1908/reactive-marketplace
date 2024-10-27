package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = "/chat/{roomId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> findMsg(@PathVariable("roomId") String roomId) {
        return chatService.findMsgByRoomId(roomId);
    }

    @PostMapping("/chat")
    public Mono<Chat> saveMsg(@RequestBody ChatSaveReqDto chatSaveReqDto) { // Mono는 한번만 리턴한다는 뜻
        return chatService.saveMsg(chatSaveReqDto);
    }
}
