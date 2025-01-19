package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.*;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
import com.hansung.reactive_marketplace.service.messaging.RedisPublisher;
import com.hansung.reactive_marketplace.util.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ProductService productService;

    private final UserService userService;

    private final ImageService imageService;

    private final RedisPublisher redisPublisher;

    public ChatServiceImpl(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, ProductService productService, UserService userService, ImageService imageService, RedisPublisher redisPublisher) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.productService = productService;
        this.userService = userService;
        this.imageService = imageService;
        this.redisPublisher = redisPublisher;
    }

    public Flux<Chat> findMsgByRoomId(String roomId) {
        return chatRepository.findMsgByRoomId(roomId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.CHAT_NOT_FOUND)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChatRoomResDto> openChat(String productId, String sellerId, String buyerId, Authentication authentication, ChatClickPage clickPage) {
        return Mono.just(AuthUtils.getAuthenticationUser(authentication))
                .flatMap(authUser -> {
                    if (authUser.getId().equals(sellerId) && clickPage.equals(ChatClickPage.DETAIL)) {
                        return Mono.error(new ApiException(ExceptionMessage.SELLER_SAME_AS_LOGGED_IN_USER));
                    }

                    String senderId = authUser.getId().equals(sellerId) ? sellerId : buyerId;
                    String receiverId = authUser.getId().equals(sellerId) ? buyerId : sellerId;

                    return userService.findUserById(receiverId)
                            .flatMap(receiver -> chatRoomRepository.findChatRoom(productId, buyerId)
                                    .flatMap(chatRoom -> imageService.findProfileImageById(receiverId)
                                            .map(image -> new ChatRoomResDto(
                                                    chatRoom.getId(),
                                                    senderId,
                                                    receiverId,
                                                    receiver.getNickname(),
                                                    image.getThumbnailPath()
                                            )))
                                    .switchIfEmpty(Mono.defer(() -> productService.findProductById(productId)
                                            .flatMap(product -> {
                                                ChatRoom newChatRoom = new ChatRoom.Builder()
                                                        .productId(productId)
                                                        .sellerId(sellerId)
                                                        .buyerId(buyerId)
                                                        .build();

                                                return chatRoomRepository.save(newChatRoom)
                                                        .flatMap(savedChatRoom ->
                                                                imageService.findProfileImageById(receiverId)
                                                                        .map(image -> new ChatRoomResDto(
                                                                                savedChatRoom.getId(),
                                                                                senderId,
                                                                                receiverId,
                                                                                receiver.getNickname(),
                                                                                image.getThumbnailPath()
                                                                        ))
                                                        );
                                            })
                                    )))
                            .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)));
                })
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.CHAT_ROOM_CREATION_FAILED));
    }

    public Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto) {
        Chat chat = new Chat.Builder()
                .msg(chatSaveReqDto.msg())
                .senderId(chatSaveReqDto.senderId())
                .receiverId(chatSaveReqDto.receiverId())
                .roomId(chatSaveReqDto.roomId())
                .build();

        return chatRepository.save(chat)
                .flatMap(savedMessage -> redisPublisher.publish(chatSaveReqDto.receiverId(), savedMessage.getMsg())
                        .thenReturn(savedMessage)) // Redis를 통해 메시지 발행
                .onErrorMap(e -> new ApiException(ExceptionMessage.CHAT_SAVE_FAILED));
    }

    public Flux<ChatRoomListResDto> findChatRoomListBySeller(Authentication authentication) {
        return chatRoomRepository.findChatRoomListBySeller(AuthUtils.getAuthenticationUser(authentication).getId())
                .flatMap(chatRoom -> createChatRoomListResponse(chatRoom));
    }

    public Flux<ChatRoomListResDto> findChatRoomListByBuyer(Authentication authentication) {
        return chatRoomRepository.findChatRoomListByBuyer(AuthUtils.getAuthenticationUser(authentication).getId())
                .flatMap(chatRoom -> createChatRoomListResponse(chatRoom));
    }

    public Mono<ChatRoomListResDto> createChatRoomListResponse(ChatRoom chatRoom) {
        return Mono.zip(productService.findProductById(chatRoom.getProductId()),
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
        }).onErrorResume(e -> Mono.error(new ApiException(ExceptionMessage.CHAT_ROOM_INFO_FETCH_FAILED)));
    }
}
