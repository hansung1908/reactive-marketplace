package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.UserProfileResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.util.AuthUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ImageService imageService;

    private final RedisCacheManager redisCacheManager;

    private final TransactionalOperator transactionalOperator;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           ImageService imageService,
                           RedisCacheManager redisCacheManager,
                           TransactionalOperator transactionalOperator) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.imageService = imageService;
        this.redisCacheManager = redisCacheManager;
        this.transactionalOperator = transactionalOperator;
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
                .as(transactionalOperator::transactional)
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)));
    }

    public Mono<User> findUserById(String userId) {
        return redisCacheManager.getOrFetch(
                "user:" + userId,
                User.class,
                userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND))),
                Duration.ofHours(1)
        );
    }

    public Mono<UserProfileResDto> findUserProfile(Authentication authentication) {
        return imageService.findProfileImageByIdWithCache(AuthUtils.getAuthenticationUser(authentication).getId())
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
                .flatMap(user -> userRepository.updateUser(
                                userUpdateReqDto.id(),
                                userUpdateReqDto.nickname(),
                                Optional.ofNullable(userUpdateReqDto.password())
                                        .filter(pwd -> !pwd.isEmpty())
                                        .map(pwd -> bCryptPasswordEncoder.encode(pwd))
                                        .orElse(user.getPassword()),
                                userUpdateReqDto.email()
                        )
                )
                .then(Mono.justOrEmpty(image)
                        .flatMap(img -> imageService.findProfileImageById(userUpdateReqDto.id())
                                .filter(findImg -> !findImg.getImagePath().equals("/img/profile.png"))
                                .switchIfEmpty(Mono.defer(() -> imageService.uploadImage(img, userUpdateReqDto.id(), userUpdateReqDto.imageSource())))
                                .flatMap(existingImage -> imageService.deleteProfileImageById(userUpdateReqDto.id()))
                                .then(imageService.uploadImage(img, userUpdateReqDto.id(), userUpdateReqDto.imageSource()))
                        )
                )
                .as(transactionalOperator::transactional)
                .then(Mono.defer(() ->redisCacheManager.deleteValue("user:" + userUpdateReqDto.id())))
                .then()
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto) {
        return userRepository.findById(userDeleteReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)))
                .flatMap(user -> userRepository.deleteById(user.getId()).thenReturn(user))
                .flatMap(user -> imageService.findProfileImageById(user.getId())
                        .flatMap(image -> imageService.deleteProfileImageById(user.getId()))
                        .switchIfEmpty(Mono.empty())
                        .thenReturn(user)
                )
                .as(transactionalOperator::transactional)
                .then(Mono.defer(() ->redisCacheManager.deleteValue("user:" + userDeleteReqDto.id())))
                .then()
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }
}
