package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
                .flatMap(exist -> {
                    if (exist) { // 중복이 있으면 error
                        return Mono.error(new RuntimeException("Username already exists"));
                    }

                    User user = new User.Builder()
                            .username(userSaveReqDto.username())
                            .nickname(userSaveReqDto.nickname())
                            .password(bCryptPasswordEncoder.encode(userSaveReqDto.password()))
                            .email(userSaveReqDto.email())
                            .build();

                    return userRepository.save(user) // 없으면 회원가입 진행
                            .flatMap(savedUser -> {
                                if (image != null) {
                                    return imageService.uploadImage(image, savedUser.getId(), userSaveReqDto.imageSource())
                                            .thenReturn(savedUser);
                                }
                                return Mono.just(savedUser);
                            });
                });
    }

    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> findUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Mono<Void> updateUser(UserUpdateReqDto userUpdateReqDto, FilePart image) {
        return userRepository.findById(userUpdateReqDto.id())
                .flatMap(user -> {
                    String password = user.getPassword();

                    if (userUpdateReqDto.password() != null && !userUpdateReqDto.password().isEmpty()) {
                        password = bCryptPasswordEncoder.encode(userUpdateReqDto.password());
                    }

                    Mono<Void> updateUserMono = userRepository.updateUser(
                            userUpdateReqDto.id(),
                            userUpdateReqDto.nickname(),
                            password,
                            userUpdateReqDto.email()
                    );

                    if (image != null) {
                        return updateUserMono.then(
                                imageService.deleteProfileImageById(userUpdateReqDto.id())
                                        .then(imageService.uploadImage(image, userUpdateReqDto.id(), userUpdateReqDto.imageSource()))
                                        .then() // Mono<Void> 반환을 위한 then
                        );
                    } else {
                        return updateUserMono;
                    }
                });
    }

    public Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto) {
        return userRepository.deleteById(userDeleteReqDto.id());
    }
}
