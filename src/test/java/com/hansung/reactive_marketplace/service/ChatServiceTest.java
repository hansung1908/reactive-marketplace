package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.*;
import com.hansung.reactive_marketplace.dto.request.ChatSaveReqDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.RedisPublisher;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import com.hansung.reactive_marketplace.repository.ChatRoomRepository;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @Mock
    private RedisPublisher redisPublisher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ChatServiceImpl chatService;

    private String productId;
    private String sellerId;
    private String buyerId;
    private String roomId;
    private ChatRoom chatRoom;
    private Chat chat;
    private User seller;
    private User buyer;
    private Product product;
    private Image image;

    @BeforeEach
    void setUp() {
        productId = "testProduct";
        sellerId = "testSeller";
        buyerId = "testBuyer";
        roomId = "testRoom";

        // ChatRoom 설정
        chatRoom = new ChatRoom.Builder()
                .productId(productId)
                .sellerId(sellerId)
                .buyerId(buyerId)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", roomId);

        // Chat 설정
        chat = new Chat.Builder()
                .msg("Hello")
                .senderId(sellerId)
                .receiverId(buyerId)
                .roomId(roomId)
                .build();
        ReflectionTestUtils.setField(chat, "createdAt", LocalDateTime.now());

        // User 설정
        seller = new User.Builder()
                .username("seller")
                .nickname("판매자")
                .password("password")
                .email("seller@test.com")
                .build();
        ReflectionTestUtils.setField(seller, "id", sellerId);

        buyer = new User.Builder()
                .username("buyer")
                .nickname("구매자")
                .password("password")
                .email("buyer@test.com")
                .build();
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        // Product 설정
        product = new Product.Builder()
                .title("테스트 상품")
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        // Image 설정
        image = new Image.Builder()
                .imageName("test.jpg")
                .imageType(MediaType.IMAGE_JPEG_VALUE)
                .imageSize(1024L)
                .imagePath("/test/image.jpg")
                .thumbnailPath("/test/thumbnail.jpg")
                .build();
    }

    private void setupBuyerAuthentication() {
        CustomUserDetail buyerDetail = new CustomUserDetail(buyer);
        when(authentication.getPrincipal()).thenReturn(buyerDetail);
    }

    private void setupSellerAuthentication() {
        CustomUserDetail sellerDetail = new CustomUserDetail(seller);
        when(authentication.getPrincipal()).thenReturn(sellerDetail);
    }

    @Test
    void findMsgByRoomId_WhenRoomExists_ThenReturnMessages() {
        when(chatRepository.findMsgByRoomId(roomId))
                .thenReturn(Flux.just(chat));

        StepVerifier.create(chatService.findMsgByRoomId(roomId))
                .expectNext(chat)
                .verifyComplete();
    }

    @Test
    void findMsgByRoomId_WhenRoomDoesNotExist_ThenThrowApiException() {
        when(chatRepository.findMsgByRoomId(roomId))
                .thenReturn(Flux.empty());

        StepVerifier.create(chatService.findMsgByRoomId(roomId))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.CHAT_NOT_FOUND))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void openChat_WhenProductAndBuyerExist_ThenReturnChatRoom() {
        setupBuyerAuthentication();

        when(chatRoomRepository.findChatRoom(eq(productId), eq(buyerId)))
                .thenReturn(Mono.just(chatRoom));
        when(userService.findUserById(eq(sellerId)))
                .thenReturn(Mono.just(seller));
        when(imageService.findProfileImageByIdWithCache(eq(sellerId)))
                .thenReturn(Mono.just(image));

        StepVerifier.create(chatService.openChat(productId, sellerId, buyerId, authentication, ChatClickPage.DETAIL))
                .expectNextMatches(chatRoomResDto ->
                        chatRoomResDto.id().equals(roomId) &&
                        chatRoomResDto.senderId().equals(buyerId) &&
                        chatRoomResDto.receiverId().equals(sellerId) &&
                        chatRoomResDto.receiverNickname().equals(seller.getNickname()) &&
                        chatRoomResDto.receiverThumbnailPath().equals(image.getThumbnailPath())
                )
                .verifyComplete();
    }

    @Test
    void openChat_WhenSellerIsLoggedIn_ThenThrowApiException() {
        setupSellerAuthentication();

        StepVerifier.create(chatService.openChat(productId, sellerId, buyerId, authentication, ChatClickPage.DETAIL))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.SELLER_SAME_AS_LOGGED_IN_USER))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void openChat_WhenReceiverDoesNotExist_ThenThrowApiException() {
        setupBuyerAuthentication();

        when(chatRoomRepository.findChatRoom(eq(productId), eq(buyerId)))
                .thenReturn(Mono.just(chatRoom));
        when(userService.findUserById(eq(sellerId)))
                .thenReturn(Mono.empty());

        StepVerifier.create(chatService.openChat(productId, sellerId, buyerId, authentication, ChatClickPage.DETAIL))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.USER_NOT_FOUND))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void openChat_WhenChatRoomCreationFails_ThenThrowApiException() {
        setupBuyerAuthentication();

        when(chatRoomRepository.findChatRoom(eq(productId), eq(buyerId)))
                .thenReturn(Mono.empty());
        when(chatRoomRepository.save(any(ChatRoom.class)))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));
        when(userService.findUserById(eq(sellerId)))
                .thenReturn(Mono.just(seller));

        StepVerifier.create(chatService.openChat(productId, sellerId, buyerId, authentication, ChatClickPage.DETAIL))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.CHAT_ROOM_CREATION_FAILED))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void saveMsg_WhenGivenValidRequest_ThenMessageIsSaved() {
        ChatSaveReqDto chatSaveReqDto = new ChatSaveReqDto(
                "Hello",
                sellerId,
                buyerId,
                roomId
        );

        when(chatRepository.save(any(Chat.class)))
                .thenReturn(Mono.just(chat));
        when(redisPublisher.publish(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(chatService.saveMsg(chatSaveReqDto))
                .expectNext(chat)
                .verifyComplete();
    }

    @Test
    void saveMsg_WhenDatabaseErrorOccurs_ThenThrowApiException() {
        ChatSaveReqDto chatSaveReqDto = new ChatSaveReqDto(
                "Hello",
                sellerId,
                buyerId,
                roomId
        );

        when(chatRepository.save(any(Chat.class)))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(chatService.saveMsg(chatSaveReqDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.CHAT_SAVE_FAILED))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void saveMsg_WhenRedisPublishFails_ThenThrowApiException() {
        ChatSaveReqDto chatSaveReqDto = new ChatSaveReqDto(
                "Hello",
                sellerId,
                buyerId,
                roomId
        );

        when(chatRepository.save(any(Chat.class)))
                .thenReturn(Mono.just(chat));
        when(redisPublisher.publish(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Redis Error")));

        StepVerifier.create(chatService.saveMsg(chatSaveReqDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.CHAT_SAVE_FAILED))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void findChatRoomListBySeller_WhenSellerExists_ThenReturnChatRooms() {
        setupSellerAuthentication();

        when(chatRoomRepository.findChatRoomListBySeller(eq(sellerId)))
                .thenReturn(Flux.just(chatRoom));
        when(productService.findProductById(eq(productId)))
                .thenReturn(Mono.just(product));
        when(chatRepository.findRecentChat(eq(roomId)))
                .thenReturn(Mono.just(chat));
        when(imageService.findProductImageById(eq(productId)))
                .thenReturn(Mono.just(image));
        when(userService.findUserById(eq(sellerId)))
                .thenReturn(Mono.just(seller));
        when(userService.findUserById(eq(buyerId)))
                .thenReturn(Mono.just(buyer));

        StepVerifier.create(chatService.findChatRoomListBySeller(authentication))
                .expectNextMatches(chatRoomListResDto ->
                        chatRoomListResDto.productId().equals(productId) &&
                        chatRoomListResDto.productTitle().equals(product.getTitle()) &&
                        chatRoomListResDto.sellerId().equals(sellerId) &&
                        chatRoomListResDto.sellerNickname().equals(seller.getNickname()) &&
                        chatRoomListResDto.buyerId().equals(buyerId) &&
                        chatRoomListResDto.buyerNickname().equals(buyer.getNickname()) &&
                        chatRoomListResDto.recentMsg().equals(chat.getMsg()) &&
                        chatRoomListResDto.thumbnailPath().equals(image.getThumbnailPath())
                )
                .verifyComplete();
    }

    @Test
    void findChatRoomListBySeller_WhenDatabaseErrorOccurs_ThenThrowApiException() {
        setupSellerAuthentication();

        when(chatRoomRepository.findChatRoomListBySeller(eq(sellerId)))
                .thenReturn(Flux.just(chatRoom));
        when(productService.findProductById(eq(productId)))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(chatService.findChatRoomListBySeller(authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.CHAT_ROOM_INFO_FETCH_FAILED))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void findChatRoomListByBuyer_WhenBuyerExists_ThenReturnChatRooms() {
        setupBuyerAuthentication();

        when(chatRoomRepository.findChatRoomListByBuyer(eq(buyerId)))
                .thenReturn(Flux.just(chatRoom));
        when(productService.findProductById(eq(productId)))
                .thenReturn(Mono.just(product));
        when(chatRepository.findRecentChat(eq(roomId)))
                .thenReturn(Mono.just(chat));
        when(imageService.findProductImageById(eq(productId)))
                .thenReturn(Mono.just(image));
        when(userService.findUserById(eq(sellerId)))
                .thenReturn(Mono.just(seller));
        when(userService.findUserById(eq(buyerId)))
                .thenReturn(Mono.just(buyer));

        StepVerifier.create(chatService.findChatRoomListByBuyer(authentication))
                .expectNextMatches(chatRoomListResDto ->
                        chatRoomListResDto.productId().equals(productId) &&
                        chatRoomListResDto.productTitle().equals(product.getTitle()) &&
                        chatRoomListResDto.sellerId().equals(sellerId) &&
                        chatRoomListResDto.sellerNickname().equals(seller.getNickname()) &&
                        chatRoomListResDto.buyerId().equals(buyerId) &&
                        chatRoomListResDto.buyerNickname().equals(buyer.getNickname()) &&
                        chatRoomListResDto.recentMsg().equals(chat.getMsg()) &&
                        chatRoomListResDto.thumbnailPath().equals(image.getThumbnailPath())
                )
                .verifyComplete();
    }

    @Test
    void findChatRoomListByBuyer_WhenDatabaseErrorOccurs_ThenThrowApiException() {
        setupBuyerAuthentication();

        when(chatRoomRepository.findChatRoomListByBuyer(eq(buyerId)))
                .thenReturn(Flux.just(chatRoom));
        when(productService.findProductById(eq(productId)))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(chatService.findChatRoomListByBuyer(authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.CHAT_ROOM_INFO_FETCH_FAILED))
                .verify(Duration.ofSeconds(1));
    }
}
