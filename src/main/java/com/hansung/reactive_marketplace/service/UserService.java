package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ImageService imageService;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder, ImageService imageService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.imageService = imageService;
    }

    public Mono<User> saveUser(UserSaveReqDto userSaveReqDto, MultipartFile image) {
        User user = new User.Builder()
                .username(userSaveReqDto.getUsername())
                .nickname(userSaveReqDto.getNickname())
                .password(bCryptPasswordEncoder.encode(userSaveReqDto.getPassword()))
                .email(userSaveReqDto.getEmail())
                .build();

        return userRepository.save(user)
                .doOnNext(savedUser -> {
                    if (image != null) {
                        imageService.uploadImage(image, savedUser.getId(), userSaveReqDto.getImageSource());
                    }
                });
    }

    public Mono<Void> updateUser(UserUpdateReqDto userUpdateReqDto) {
        return userRepository.findById(userUpdateReqDto.getId())
                .flatMap(user -> {
                    String password = user.getPassword();

                    if (userUpdateReqDto.getPassword() != null && !userUpdateReqDto.getPassword().isEmpty()) {
                        password = bCryptPasswordEncoder.encode(userUpdateReqDto.getPassword());
                    }

                    return userRepository.updateUser(userUpdateReqDto.getId(), userUpdateReqDto.getNickname(), password, userUpdateReqDto.getEmail());
                });
    }

    public Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto) {
        return userRepository.deleteById(userDeleteReqDto.getId());
    }
}
