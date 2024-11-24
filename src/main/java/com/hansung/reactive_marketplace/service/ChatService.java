package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.*;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ProductRepository productRepository;

    private final UserService userService;

    private final ImageService imageService;

    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, ProductRepository productRepository, UserService userService, ImageService imageService) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.imageService = imageService;
    }

    public Flux<Chat> findMsgByRoomId(String roomId) {
        return chatRepository.findMsgByRoomId(roomId)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChatRoomResDto> openChat(String productId, String sellerId, String buyerId) {
        return chatRoomRepository.findChatRoom(productId, sellerId, buyerId)
                .flatMap(chatRoom -> ReactiveSecurityContextHolder.getContext()
                        .flatMap(securityContext -> userService.findUserByUsername(securityContext.getAuthentication().getName()) // 로그인된 사용자 username으로 user 정보 가져오기
                                .flatMap(user -> imageService.findProfileImageById(user.getId())
                                        .map(image -> new ChatRoomResDto(
                                                chatRoom.getId(),
                                                chatRoom.getSellerId(),
                                                chatRoom.getBuyerId(),
                                                image.getThumbnailPath()
                                        ))
                                )
                        ))
                .switchIfEmpty(Mono.defer(() -> { // 새로운 채팅방 생성 로직
                    ChatRoom chatRoom = new ChatRoom.Builder()
                            .productId(productId)
                            .sellerId(sellerId)
                            .buyerId(buyerId)
                            .build();

                    return chatRoomRepository.save(chatRoom)
                            .flatMap(savedChatRoom -> ReactiveSecurityContextHolder.getContext()
                                    .flatMap(securityContext -> userService.findUserByUsername(securityContext.getAuthentication().getName()) // 로그인된 사용자 username으로 user 정보 가져오기
                                            .flatMap(user -> imageService.findProfileImageById(user.getId())
                                                    .map(image -> new ChatRoomResDto(
                                                                    savedChatRoom.getId(),
                                                                    savedChatRoom.getSellerId(),
                                                                    savedChatRoom.getBuyerId(),
                                                                    image.getThumbnailPath()
                                                            )
                                                    )
                                            )
                                    )
                            );
                }));
    }

    public Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto) {
        Chat chat = new Chat.Builder()
                .msg(chatSaveReqDto.msg())
                .senderId(chatSaveReqDto.senderId())
                .receiverId(chatSaveReqDto.receiverId())
                .roomId(chatSaveReqDto.roomId())
                .build();

        return chatRepository.save(chat);
    }

    public Flux<ChatRoomListResDto> findChatRoomList(String userId) {
        return chatRoomRepository.findChatRoomListBySellerOrBuyer(userId)
                .flatMap(chatRoom -> Mono.zip(
                        productRepository.findById(chatRoom.getProductId()),
                        chatRepository.findRecentChat(chatRoom.getId()),
                        imageService.findProductImageById(chatRoom.getProductId()),
                        userService.findUserById(chatRoom.getSellerId()),
                        userService.findUserById(chatRoom.getBuyerId())
                ).map(tuple -> {
                    Product product = tuple.getT1();
                    Chat chat = tuple.getT2();
                    Image image = tuple.getT3();
                    User seller = tuple.getT4();
                    User buyer = tuple.getT5();

                    return new ChatRoomListResDto(
                            chatRoom.getProductId(),
                            product.getTitle(),
                            chatRoom.getSellerId(),
                            seller.getNickname(),
                            chatRoom.getBuyerId(),
                            buyer.getNickname(),
                            chat.getMsg(),
                            chat.getCreatedAt(),
                            image.getThumbnailPath());
                }));
    }
}
