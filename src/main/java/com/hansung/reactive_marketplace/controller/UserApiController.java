package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/save")
    public Mono<User> save(@RequestPart("user") UserSaveReqDto userSaveReqDto,
                           @RequestPart(value = "image", required = false) MultipartFile image) {
        return userService.saveUser(userSaveReqDto, image);
    }

    @PutMapping("/user/update")
    public Mono<Void> update(@RequestBody UserUpdateReqDto userUpdateReqDto) {
        return userService.updateUser(userUpdateReqDto);
    }

    @DeleteMapping("/user/delete")
    public Mono<Void> delete(@RequestBody UserDeleteReqDto userDeleteReqDto) {
        return userService.deleteUser(userDeleteReqDto);
    }
}
