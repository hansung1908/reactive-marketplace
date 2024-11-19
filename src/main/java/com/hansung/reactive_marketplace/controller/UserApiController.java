package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.service.UserService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/save")
    public Mono<User> save(@RequestPart("user") UserSaveReqDto userSaveReqDto,
                           @RequestPart(value = "image", required = false) FilePart image) {
        return userService.saveUser(userSaveReqDto, image);
    }

    @PutMapping("/user/update")
    public Mono<Void> update(@RequestPart("user") UserUpdateReqDto userUpdateReqDto,
                             @RequestPart(value = "image", required = false) FilePart image) {
        return userService.updateUser(userUpdateReqDto, image);
    }

    @DeleteMapping("/user/delete")
    public Mono<Void> delete(@RequestBody UserDeleteReqDto userDeleteReqDto) {
        return userService.deleteUser(userDeleteReqDto);
    }
}
