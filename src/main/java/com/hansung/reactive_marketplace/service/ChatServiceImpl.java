package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.domain.ChatClickPage;
import com.hansung.reactive_marketplace.domain.ChatRoom;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.RedisPublisher;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
import com.hansung.reactive_marketplace.util.AuthUtils;
import com.hansung.reactive_marketplace.util.DateTimeUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.util.retry.Retry;

import java.time.Duration;

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
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .maxBackoff(Duration.ofMillis(500))
                        .jitter(0.3))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChatRoomResDto> openChat(String productId, String sellerId, String buyerId, Authentication authentication, ChatClickPage clickPage) {
        return Mono.just(AuthUtils.getAuthenticationUser(authentication))
                .filter(authUser -> !(authUser.getId().equals(sellerId) && clickPage.equals(ChatClickPage.DETAIL)))
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.SELLER_SAME_AS_LOGGED_IN_USER)))
                .flatMap(authUser -> {
                    String senderId = authUser.getId().equals(sellerId) ? sellerId : buyerId;
                    String receiverId = authUser.getId().equals(sellerId) ? buyerId : sellerId;

                    return Mono.zip(
                                    chatRoomRepository.findChatRoom(productId, buyerId)
                                            .switchIfEmpty(Mono.defer(() -> createNewChatRoom(productId, sellerId, buyerId))),
                                    userService.findUserById(receiverId)
                                            .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)))
                            )
                            .flatMap(TupleUtils.function((chatRoom, receiver) ->
                                    createChatRoomResponse(chatRoom, senderId, receiverId, receiver)
                            ));
                })
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.CHAT_ROOM_CREATION_FAILED));
    }

    private Mono<ChatRoom> createNewChatRoom(String productId, String sellerId, String buyerId) {
        return Mono.just(new ChatRoom.Builder()
                        .productId(productId)
                        .sellerId(sellerId)
                        .buyerId(buyerId)
                        .build())
                .flatMap(newChatRoom -> chatRoomRepository.save(newChatRoom));
    }

    private Mono<ChatRoomResDto> createChatRoomResponse(ChatRoom chatRoom, String senderId, String receiverId, User receiver) {
        return imageService.findProfileImageByIdWithCache(receiverId)
                .map(image -> new ChatRoomResDto(
                        chatRoom.getId(),
                        senderId,
                        receiverId,
                        receiver.getNickname(),
                        image.getThumbnailPath()
                ));
    }

    public Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto) {
        return Mono.just(new Chat.Builder()
                        .msg(chatSaveReqDto.msg())
                        .senderId(chatSaveReqDto.senderId())
                        .receiverId(chatSaveReqDto.receiverId())
                        .roomId(chatSaveReqDto.roomId())
                        .build()
                )
                .flatMap(chat -> chatRepository.save(chat))
                .flatMap(savedMessage ->
                        redisPublisher.publish(chatSaveReqDto.receiverId(), savedMessage.getMsg())
                                .thenReturn(savedMessage)) // Redis를 통해 메시지 발행
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.CHAT_SAVE_FAILED));
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
        return Mono.zip(
                        productService.findProductById(chatRoom.getProductId()),
                        chatRepository.findRecentChat(chatRoom.getId()),
                        imageService.findProductImageById(chatRoom.getProductId()),
                        userService.findUserById(chatRoom.getSellerId()),
                        userService.findUserById(chatRoom.getBuyerId())
                )
                .map(TupleUtils.function((product, chat, image, seller, buyer) ->
                        new ChatRoomListResDto(
                                chatRoom.getProductId(),
                                product.getTitle(),
                                chatRoom.getSellerId(),
                                seller.getNickname(),
                                chatRoom.getBuyerId(),
                                buyer.getNickname(),
                                chat.getMsg(),
                                DateTimeUtils.format(chat.getCreatedAt()),
                                image.getThumbnailPath()
                        )
                ))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.CHAT_ROOM_INFO_FETCH_FAILED));
    }
}
