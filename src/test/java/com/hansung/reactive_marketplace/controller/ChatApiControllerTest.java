package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatApiControllerTest {
    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatApiController chatApiController;

    @Test
    void findMsg_ShouldReturnChatFlux() {
        // Given
        String roomId = "testId";
        Chat chat1 = new Chat.Builder()
                .msg("test msg")
                .senderId("testSenderId")
                .receiverId("testReceiverId")
                .roomId("testId")
                .build();
        Chat chat2 = new Chat.Builder()
                .msg("test msg")
                .senderId("testSenderId")
                .receiverId("testReceiverId")
                .roomId("testId")
                .build();;
        when(chatService.findMsgByRoomId(roomId))
                .thenReturn(Flux.just(chat1, chat2));

        // When & Then
        StepVerifier.create(chatApiController.findMsg(roomId))
                .expectNext(chat1)
                .expectNext(chat2)
                .verifyComplete();
    }

    @Test
    void saveMsg_ShouldReturnCreatedStatus() {
        // Given
        ChatSaveReqDto chatSaveReqDto = new ChatSaveReqDto(
                "test msg",
                "testSenderId",
                "testReceiverId",
                "testId"
        );
        when(chatService.saveMsg(any(ChatSaveReqDto.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(chatApiController.saveMsg(chatSaveReqDto))
                .verifyComplete();
    }
}

