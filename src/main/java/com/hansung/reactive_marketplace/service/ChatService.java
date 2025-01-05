package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.domain.ChatClickPage;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {

    // 특정 채팅방의 메시지를 가져오는 메소드
    Flux<Chat> findMsgByRoomId(String roomId);

    Mono<ChatRoomResDto> openChat(String productId, String SellerId, String buyerId, Authentication authentication, ChatClickPage clickPage);

    // 채팅 메시지를 저장하는 메소드
    Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto);

    // 판매자의 채팅방 리스트를 가져오는 메소드
    Flux<ChatRoomListResDto> findChatRoomListBySeller(Authentication authentication);

    // 구매자의 채팅방 리스트를 가져오는 메소드
    Flux<ChatRoomListResDto> findChatRoomListByBuyer(Authentication authentication);
}
