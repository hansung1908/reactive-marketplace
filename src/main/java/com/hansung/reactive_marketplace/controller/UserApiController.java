package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/save")
    public Mono<User> save(@RequestBody UserSaveReqDto userSaveReqDto) {
        return userService.saveUser(userSaveReqDto);
    }
}
