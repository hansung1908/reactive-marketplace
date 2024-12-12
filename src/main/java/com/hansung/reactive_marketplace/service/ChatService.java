package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.BuyerChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.dto.response.SellerChatRoomListResDto;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {

    // 특정 채팅방의 메시지를 가져오는 메소드
    Flux<Chat> findMsgByRoomId(String roomId);

    Mono<ChatRoomResDto> openChatBySeller(String productId, String buyerId, Authentication authentication);

    // 채팅방을 여는 메소드
    Mono<ChatRoomResDto> openChatByBuyer(String productId, Authentication authentication);

    // 채팅 메시지를 저장하는 메소드
    Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto);

    // 판매자의 채팅방 리스트를 가져오는 메소드
    Flux<SellerChatRoomListResDto> findChatRoomListBySeller(Authentication authentication);

    // 구매자의 채팅방 리스트를 가져오는 메소드
    Flux<BuyerChatRoomListResDto> findChatRoomListByBuyer(Authentication authentication);
}
