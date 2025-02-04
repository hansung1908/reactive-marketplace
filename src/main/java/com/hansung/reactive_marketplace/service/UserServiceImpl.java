package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.UserProfileResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.ReactiveRedisHandler;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.util.AuthUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ImageService imageService;

    private final ReactiveRedisHandler reactiveRedisHandler;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           ImageService imageService,
                           ReactiveRedisHandler reactiveRedisHandler) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.imageService = imageService;
        this.reactiveRedisHandler = reactiveRedisHandler;
    }

    public Mono<User> saveUser(UserSaveReqDto userSaveReqDto, FilePart image) {
        return Mono.just(new User.Builder()
                        .username(userSaveReqDto.username())
                        .nickname(userSaveReqDto.nickname())
                        .password(bCryptPasswordEncoder.encode(userSaveReqDto.password()))
                        .email(userSaveReqDto.email())
                        .build())
                .flatMap(user -> userRepository.existsByUsername(user.getUsername())
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USERNAME_ALREADY_EXISTS)))
                        .thenReturn(user))
                .flatMap(user -> userRepository.save(user))
                .flatMap(savedUser ->
                        Mono.justOrEmpty(image)
                                .flatMap(img -> imageService.uploadImage(img, savedUser.getId(), userSaveReqDto.imageSource())
                                        .thenReturn(savedUser))
                                .defaultIfEmpty(savedUser))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)));
    }

    public Mono<User> findUserById(String userId) {
        return reactiveRedisHandler.getOrFetch(
                "user:" + userId,
                User.class,
                userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND))),
                Duration.ofHours(1)
        );
    }

    public Mono<UserProfileResDto> findUserProfile(Authentication authentication) {
        return imageService.findProfileImageById(AuthUtils.getAuthenticationUser(authentication).getId())
                .flatMap(image -> findUserById(AuthUtils.getAuthenticationUser(authentication).getId())
                        .map(user -> new UserProfileResDto(
                                user.getUsername(),
                                user.getNickname(),
                                user.getEmail(),
                                image.getImagePath()
                        )))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<Void> updateUser(UserUpdateReqDto userUpdateReqDto, FilePart image) {
        return userRepository.findById(userUpdateReqDto.id())
                .flatMap(user -> {
                    String password = Optional.ofNullable(userUpdateReqDto.password())
                            .filter(pwd -> !pwd.isEmpty())
                            .map(pwd -> bCryptPasswordEncoder.encode(pwd))
                            .orElse(user.getPassword());

                    Mono<Void> updateUserMono = userRepository.updateUser(
                            userUpdateReqDto.id(),
                            userUpdateReqDto.nickname(),
                            password,
                            userUpdateReqDto.email()
                    );

                    Mono<Void> updateImageMono = Mono.justOrEmpty(image)
                            .flatMap(img -> imageService.findProfileImageById(userUpdateReqDto.id())
                                    .filter(findImg -> !findImg.getImagePath().equals("/img/profile.png"))
                                    .switchIfEmpty(Mono.defer(() -> imageService.uploadImage(img, userUpdateReqDto.id(), userUpdateReqDto.imageSource())))
                                    .flatMap(existingImage -> imageService.deleteProfileImageById(userUpdateReqDto.id()))
                                    .then(imageService.uploadImage(img, userUpdateReqDto.id(), userUpdateReqDto.imageSource()))
                            )
                            .then();

                    Mono<Boolean> deleteCacheMono = reactiveRedisHandler.deleteValue("user:" + userUpdateReqDto.id());

                    return Mono.when(updateUserMono, updateImageMono, deleteCacheMono);
                })
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }


    public Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto) {
        return userRepository.findById(userDeleteReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)))
                .flatMap(user ->
                        imageService.findProfileImageById(user.getId())
                                .flatMap(image -> Mono.when(
                                        userRepository.deleteById(user.getId()),
                                        imageService.deleteProfileImageById(user.getId()),
                                        reactiveRedisHandler.deleteValue("user:" + user.getId())
                                ))
                                .switchIfEmpty(Mono.when(
                                        userRepository.deleteById(user.getId()),
                                        reactiveRedisHandler.deleteValue("user:" + user.getId())
                                ))
                )
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }
}
