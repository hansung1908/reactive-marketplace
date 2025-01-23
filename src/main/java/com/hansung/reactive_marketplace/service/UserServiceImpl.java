package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.UserProfileResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.util.AuthUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ImageService imageService;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder, ImageService imageService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.imageService = imageService;
    }

    public Mono<User> saveUser(UserSaveReqDto userSaveReqDto, FilePart image) {
        return userRepository.existsByUsername(userSaveReqDto.username())
                .filter(exist -> !exist) // 중복이 있는 상태를 empty 상태로 변환
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USERNAME_ALREADY_EXISTS)))
                .flatMap(nonExist -> { // 없으면 회원가입 진행
                    return Mono.fromCallable(() -> new User.Builder()
                                    .username(userSaveReqDto.username())
                                    .nickname(userSaveReqDto.nickname())
                                    .password(bCryptPasswordEncoder.encode(userSaveReqDto.password()))
                                    .email(userSaveReqDto.email())
                                    .build())
                            .flatMap(user -> Mono.zip(
                                    userRepository.save(user),
                                    Mono.justOrEmpty(image)
                                            .flatMap(img -> imageService.uploadImage(img, user.getId(), userSaveReqDto.imageSource()))
                                            .switchIfEmpty(Mono.empty())
                            ))
                            .map(tuple -> tuple.getT1());
                })
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)));
    }

    public Mono<User> findUserById(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)));
    }

    @Override
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
                        e -> new ApiException(ExceptionMessage.USER_NOT_FOUND));
    }

    public Mono<Void> updateUser(UserUpdateReqDto userUpdateReqDto, FilePart image) {
        return userRepository.findById(userUpdateReqDto.id())
                .flatMap(user -> {
                    String password = Optional.ofNullable(userUpdateReqDto.password())
                            .filter(pwd -> !pwd.isEmpty())
                            .map(pwd -> bCryptPasswordEncoder.encode(pwd))
                            .orElse(user.getPassword());

                    return userRepository.updateUser(
                                    userUpdateReqDto.id(),
                                    userUpdateReqDto.nickname(),
                                    password,
                                    userUpdateReqDto.email()
                            )
                            .then(Mono.justOrEmpty(image)
                                    .flatMap(img -> imageService.findProfileImageById(userUpdateReqDto.id())
                                            .filter(findImg -> !findImg.getImagePath().equals("/img/profile.png"))
                                            .flatMap(existingImage -> imageService.deleteProfileImageById(userUpdateReqDto.id()))
                                            .then(imageService.uploadImage(img, userUpdateReqDto.id(), userUpdateReqDto.imageSource()))
                                            .switchIfEmpty(imageService.uploadImage(img, userUpdateReqDto.id(), userUpdateReqDto.imageSource()))
                                    )
                            )
                            .then();
                })
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto) {
        return userRepository.findById(userDeleteReqDto.id()) // deleteById는 id 유무와 상관없이 실행하므로 체크 필요
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.USER_NOT_FOUND)))
                .flatMap(user -> userRepository.deleteById(user.getId()))
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }
}
