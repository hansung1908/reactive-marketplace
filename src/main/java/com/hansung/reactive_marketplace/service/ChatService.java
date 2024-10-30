package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.domain.ChatRoom;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
import com.hansung.reactive_marketplace.util.ChatUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatUtils chatUtils;

    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, ChatUtils chatUtils) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatUtils = chatUtils;
    }

    public Flux<Chat> findMsgByRoomId(String roomId) {
        return chatRepository.findMsgByRoomId(roomId)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChatRoom> openChat(String productId, String seller, String buyer) {
        return chatRoomRepository.findChatRoom(productId, seller)
                .flatMap(chatRoom -> { // 기존 채팅방이 있을 경우
                    return Mono.just(chatRoom);
                })
                .switchIfEmpty(Mono.defer(() -> { // 새로운 채팅방 생성 로직
                    ChatRoom chatRoom = new ChatRoom.Builder()
                            .productId(productId)
                            .seller(seller)
                            .buyer(buyer)
                            .build();

                    return chatRoomRepository.save(chatRoom)
                            .flatMap(savedChatRoom -> {
                                return chatUtils.saveDummyChat(savedChatRoom.getId()) // 더미 채팅 메시지 저장
                                        .then(Mono.just(savedChatRoom)); // 저장된 채팅방 반환
                            });
                }));
    }

    public Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto) {
        Chat chat = new Chat.Builder()
                .msg(chatSaveReqDto.getMsg())
                .sender(chatSaveReqDto.getSender())
                .receiver(chatSaveReqDto.getReceiver())
                .roomId(chatSaveReqDto.getRoomId())
                .build();

        return chatRepository.save(chat);
    }

    public Flux<ChatRoom> findChatRoomList(String nickname) {
        return chatRoomRepository.findChatRoomListBySellerOrBuyer(nickname)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
