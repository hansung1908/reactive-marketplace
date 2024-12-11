package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.*;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
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

    public ChatServiceImpl(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, ProductService productService, UserService userService, ImageService imageService) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.productService = productService;
        this.userService = userService;
        this.imageService = imageService;
    }

    public Flux<Chat> findMsgByRoomId(String roomId) {
        return chatRepository.findMsgByRoomId(roomId)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChatRoomResDto> openChatByBuyerId(String productId, Authentication authentication) {
        return chatRoomRepository.findChatRoom(productId, AuthUtils.getAuthenticationUser(authentication).getId())
                .flatMap(chatRoom -> imageService.findProfileImageById(AuthUtils.getAuthenticationUser(authentication).getId())
                        .map(image -> new ChatRoomResDto(
                                chatRoom.getId(),
                                chatRoom.getSellerId(),
                                chatRoom.getBuyerId(),
                                image.getThumbnailPath()
                        ))
                        .switchIfEmpty(Mono.defer(() ->
                                productService.findProductById(productId)
                                        .flatMap(product -> {
                                            ChatRoom newChatRoom = new ChatRoom.Builder()
                                                    .productId(productId)
                                                    .sellerId(product.getId())
                                                    .buyerId(AuthUtils.getAuthenticationUser(authentication).getId())
                                                    .build();

                                            return chatRoomRepository.save(newChatRoom)
                                                    .flatMap(savedChatRoom ->
                                                            imageService.findProfileImageById(AuthUtils.getAuthenticationUser(authentication).getId())
                                                                    .map(image -> new ChatRoomResDto(
                                                                            savedChatRoom.getId(),
                                                                            savedChatRoom.getSellerId(),
                                                                            savedChatRoom.getBuyerId(),
                                                                            image.getThumbnailPath()
                                                                    ))
                                                    );
                                        })
                        )));
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

    public Flux<ChatRoomListResDto> findChatRoomList(Authentication authentication) {
        return chatRoomRepository.findChatRoomListBySellerOrBuyer(AuthUtils.getAuthenticationUser(authentication).getId())
                .flatMap(chatRoom -> Mono.zip(
                        productService.findProductById(chatRoom.getProductId()),
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
