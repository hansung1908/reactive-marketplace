package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private RedisCacheManager redisCacheManager;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private UserService userService;

    private String userId;
    private String username;
    private String nickname;
    private String password;
    private String email;
    private User user;
    private Image image;
    private UserSaveReqDto userSaveReqDto;
    private UserUpdateReqDto userUpdateReqDto;
    private UserDeleteReqDto userDeleteReqDto;
    private FilePart filePart;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, bCryptPasswordEncoder, imageService, redisCacheManager);

        userId = "testUser";
        username = "testUsername";
        nickname = "테스트 유저";
        password = "password";
        email = "test@test.com";

        // User 설정
        user = new User.Builder()
                .username(username)
                .nickname(nickname)
                .password(password)
                .email(email)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        // Image 설정
        image = new Image.Builder()
                .imageName("test.jpg")
                .imageType(MediaType.IMAGE_JPEG_VALUE)
                .imageSize(1024L)
                .imagePath("/test/image.jpg")
                .thumbnailPath("/test/thumbnail.jpg")
                .build();

        // UserSaveReqDto 설정
        userSaveReqDto = new UserSaveReqDto(
                username,
                nickname,
                password,
                email,
                ImageSource.PROFILE
        );

        // UserUpdateReqDto 설정
        userUpdateReqDto = new UserUpdateReqDto(
                userId,
                nickname,
                password,
                email,
                ImageSource.PROFILE
        );

        // UserDeleteReqDto 설정
        userDeleteReqDto = new UserDeleteReqDto(userId);

        // FilePart 설정
        filePart = mock(FilePart.class);
    }

    @Test
    void givenUserSaveReqDto_whenSaveUser_thenSuccess() {
        // given
        when(bCryptPasswordEncoder.encode(password))
                .thenReturn("encodedPassword");
        when(userRepository.existsByUsername(username))
                .thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(user));
        when(imageService.uploadImage(eq(filePart), eq(userId), eq(ImageSource.PROFILE)))
                .thenReturn(Mono.empty());

        // when & then
        StepVerifier.create(userService.saveUser(userSaveReqDto, filePart))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void givenExistingUsername_whenSaveUser_thenUsernameAlreadyExists() {
        // given
        when(userRepository.existsByUsername(username))
                .thenReturn(Mono.just(true));

        // when & then
        StepVerifier.create(userService.saveUser(userSaveReqDto, filePart))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.USERNAME_ALREADY_EXISTS))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void givenUserSaveReqDto_whenSaveUser_thenSaveFailed() {
        // given
        when(bCryptPasswordEncoder.encode(password))
                .thenReturn("encodedPassword");
        when(userRepository.existsByUsername(username))
                .thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        // when & then
        StepVerifier.create(userService.saveUser(userSaveReqDto, filePart))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.INTERNAL_SERVER_ERROR))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void givenUserId_whenFindUserById_thenSuccess() {
        // given
        String cacheKey = "user:" + userId;
        when(redisCacheManager.getOrFetch(
                eq(cacheKey),
                eq(User.class),
                any(Mono.class),
                any(Duration.class)))
                .thenReturn(Mono.just(user));
        when(userRepository.findById(userId))
                .thenReturn(Mono.just(user));

        // when & then
        StepVerifier.create(userService.findUserById(userId))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void givenUserId_whenFindUserById_thenUserNotFound() {
        // given
        String cacheKey = "user:" + userId;
        when(redisCacheManager.getOrFetch(
                eq(cacheKey),
                eq(User.class),
                any(Mono.class),
                any(Duration.class)))
                .thenReturn(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)));
        when(userRepository.findById(userId))
                .thenReturn(Mono.empty());

        // when & then
        StepVerifier.create(userService.findUserById(userId))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.USER_NOT_FOUND))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void givenUsername_whenFindUserByUsername_thenSuccess() {
        // given
        when(userRepository.findByUsername(username))
                .thenReturn(Mono.just(user));

        // when & then
        StepVerifier.create(userService.findUserByUsername(username))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void givenUsername_whenFindUserByUsername_thenUserNotFound() {
        // given
        when(userRepository.findByUsername(username))
                .thenReturn(Mono.empty());

        // when & then
        StepVerifier.create(userService.findUserByUsername(username))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.USER_NOT_FOUND))
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void givenUserUpdateReqDto_whenUpdateUser_thenSuccess() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Mono.just(user));
        when(bCryptPasswordEncoder.encode(password))
                .thenReturn("encodedPassword");
        when(userRepository.updateUser(eq(userId), eq(nickname), eq("encodedPassword"), eq(email)))
                .thenReturn(Mono.empty());
        when(imageService.findProfileImageById(userId))
                .thenReturn(Mono.just(image));
        when(imageService.deleteProfileImageById(userId))
                .thenReturn(Mono.empty());
        when(imageService.uploadImage(eq(filePart), eq(userId), eq(ImageSource.PROFILE)))
                .thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue("user:" + userId))
                .thenReturn(Mono.just(true));

        // when & then
        StepVerifier.create(userService.updateUser(userUpdateReqDto, filePart))
                .verifyComplete();
    }

    @Test
    void givenUserDeleteReqDto_whenDeleteUser_thenSuccess() {
        // given
        when(userRepository.findById(userId))
                .thenReturn(Mono.just(user));
        when(imageService.findProfileImageById(userId))
                .thenReturn(Mono.just(image));
        when(userRepository.deleteById(userId))
                .thenReturn(Mono.empty());
        when(imageService.deleteProfileImageById(userId))
                .thenReturn(Mono.empty());
        when(redisCacheManager.deleteValue("user:" + userId))
                .thenReturn(Mono.just(true));

        // when & then
        StepVerifier.create(userService.deleteUser(userDeleteReqDto))
                .verifyComplete();
    }

    @Test
    void givenAuthentication_whenFindUserProfile_thenSuccess() {
        // given
        Authentication authentication = mock(Authentication.class);
        CustomUserDetail userDetail = new CustomUserDetail(user);
        when(authentication.getPrincipal()).thenReturn(userDetail);

        when(imageService.findProfileImageByIdWithCache(userId))
                .thenReturn(Mono.just(image));
        when(redisCacheManager.getOrFetch(
                eq("user:" + userId),
                eq(User.class),
                any(Mono.class),
                any(Duration.class)))
                .thenReturn(Mono.just(user));
        when(userRepository.findById(userId))
                .thenReturn(Mono.just(user));

        // when & then
        StepVerifier.create(userService.findUserProfile(authentication))
                .expectNextMatches(profile ->
                        profile.username().equals(username) &&
                        profile.nickname().equals(nickname) &&
                        profile.email().equals(email) &&
                        profile.imagePath().equals(image.getImagePath())
                )
                .verifyComplete();
    }

    @Test
    void givenAuthentication_whenFindUserProfile_thenImageNotFound() {
        // given
        Authentication authentication = mock(Authentication.class);
        CustomUserDetail userDetail = new CustomUserDetail(user);
        when(authentication.getPrincipal()).thenReturn(userDetail);

        when(imageService.findProfileImageByIdWithCache(userId))
                .thenReturn(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)));

        // when & then
        StepVerifier.create(userService.findUserProfile(authentication))
                .expectErrorMatches(throwable -> throwable instanceof ApiException &&
                        ((ApiException) throwable).getException().equals(ExceptionMessage.IMAGE_NOT_FOUND))
                .verify(Duration.ofSeconds(1));
    }
}
