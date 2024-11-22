package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.domain.ChatRoom;
import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomListResDto;
import com.hansung.reactive_marketplace.dto.response.ChatRoomResDto;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import com.hansung.reactive_marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, ProductRepository productRepository, UserRepository userRepository, ImageService imageService) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    public Flux<Chat> findMsgByRoomId(String roomId) {
        return chatRepository.findMsgByRoomId(roomId)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ChatRoomResDto> openChat(String productId, String seller, String buyer) {
        return chatRoomRepository.findChatRoom(productId, seller)
                .flatMap(chatRoom -> userRepository.findByNickname(buyer)
                        .flatMap(user -> imageService.findProfileImageById(user.getId()) // 기존 채팅방이 있을 경우
                                .map(image -> new ChatRoomResDto(
                                        chatRoom.getId(),
                                        chatRoom.getSeller(),
                                        chatRoom.getBuyer(),
                                        image.getThumbnailPath()
                                ))
                        )
                )
                .switchIfEmpty(Mono.defer(() -> { // 새로운 채팅방 생성 로직
                    ChatRoom chatRoom = new ChatRoom.Builder()
                            .productId(productId)
                            .seller(seller)
                            .buyer(buyer)
                            .build();

                    return chatRoomRepository.save(chatRoom)
                            .flatMap(savedChatRoom -> userRepository.findByNickname(buyer)
                                .flatMap(user -> imageService.findProfileImageById(user.getId())
                                        .map(image -> new ChatRoomResDto(
                                                savedChatRoom.getId(),
                                                savedChatRoom.getSeller(),
                                                savedChatRoom.getBuyer(),
                                                image.getThumbnailPath()
                                        ))
                                )
                            );
                }));
    }

    public Mono<Chat> saveMsg(ChatSaveReqDto chatSaveReqDto) {
        Chat chat = new Chat.Builder()
                .msg(chatSaveReqDto.msg())
                .sender(chatSaveReqDto.sender())
                .receiver(chatSaveReqDto.receiver())
                .roomId(chatSaveReqDto.roomId())
                .build();

        return chatRepository.save(chat);
    }

    public Flux<ChatRoomListResDto> findChatRoomList(String nickname) {
        return chatRoomRepository.findChatRoomListBySellerOrBuyer(nickname)
                .flatMap(chatRoom -> Mono.zip(
                        productRepository.findById(chatRoom.getProductId()),
                        chatRepository.findRecentChat(chatRoom.getId()),
                        imageService.findProductImageById(chatRoom.getProductId())
                ).map(tuple -> {
                    Product product = tuple.getT1();
                    Chat chat = tuple.getT2();
                    Image image = tuple.getT3();

                    return new ChatRoomListResDto(
                            chatRoom.getProductId(),
                            product.getTitle(),
                            chatRoom.getSeller(),
                            chatRoom.getBuyer(),
                            chat.getMsg(),
                            chat.getCreatedAt(),
                            image.getThumbnailPath());
                }));
    }
}
