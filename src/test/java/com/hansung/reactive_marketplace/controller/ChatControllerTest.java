package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.ChatClickPage;
import com.hansung.reactive_marketplace.domain.ChatRoom;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @Test
    void openChat_ShouldReturnChatRoomView() {
        // Given
        String productId = "1";
        String sellerId = "seller1";
        String buyerId = "buyer1";
        ChatClickPage clickPage = ChatClickPage.DETAIL;
        Authentication mockAuth = mock(Authentication.class);
        ChatRoomResDto chatRoom = new ChatRoomResDto(
                "testId",
                "testSenderId",
                "testReceiverId",
                "nickname",
                "path/to/image"
        );

        when(chatService.openChat(productId, sellerId, buyerId, mockAuth, clickPage))
                .thenReturn(Mono.just(chatRoom));

        // When & Then
        StepVerifier.create(chatController.openChat(productId, sellerId, buyerId, clickPage, mockAuth))
                .expectNextMatches(rendering ->
                        rendering.view().equals("chat/chatRoomForm") &&
                                rendering.modelAttributes().containsKey("chatRoom") &&
                                rendering.modelAttributes().get("chatRoom").equals(chatRoom)
                )
                .verifyComplete();
    }

    @Test
    void findChatRoomList_ShouldReturnChatRoomListView() {
        // Given
        Authentication mockAuth = mock(Authentication.class);
        Flux<ChatRoomListResDto> sellerChatRooms = Flux.just(new ChatRoomListResDto(
                "testId",
                "testProduct",
                "testSellerId",
                "sellerNickname",
                "testBuyerId",
                "buyerNickname",
                "test msg",
                "2024-02-21 23:27",
                "path/to/image"
        ));
        Flux<ChatRoomListResDto> buyerChatRooms = Flux.just(new ChatRoomListResDto(
                "testId",
                "testProduct",
                "testSellerId",
                "sellerNickname",
                "testBuyerId",
                "buyerNickname",
                "test msg",
                "2024-02-21 23:27",
                "path/to/image"
        ));

        when(chatService.findChatRoomListBySeller(mockAuth)).thenReturn(sellerChatRooms);
        when(chatService.findChatRoomListByBuyer(mockAuth)).thenReturn(buyerChatRooms);

        // When & Then
        StepVerifier.create(chatController.findChatRoomList(mockAuth))
                .expectNextMatches(rendering ->
                        rendering.view().equals("chat/chatRoomListForm") &&
                                rendering.modelAttributes().containsKey("sellerChatRoomList") &&
                                rendering.modelAttributes().containsKey("buyerChatRoomList")
                )
                .verifyComplete();
    }
}

