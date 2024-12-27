package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<String>> save(@RequestPart("user") UserSaveReqDto userSaveReqDto,
                                             @RequestPart(value = "image", required = false) FilePart image) {
        return userService.saveUser(userSaveReqDto, image)
                .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                        .body("User saved successfully")));
    }

    @PutMapping("/user/update")
    public Mono<ResponseEntity<String>> update(@RequestPart("user") UserUpdateReqDto userUpdateReqDto,
                                               @RequestPart(value = "image", required = false) FilePart image) {
        return userService.updateUser(userUpdateReqDto, image)
                .then(Mono.just(ResponseEntity.ok("User updated successfully")));
    }

    @DeleteMapping("/user/delete")
    public Mono<ResponseEntity<String>> delete(@RequestBody UserDeleteReqDto userDeleteReqDto) {
        return userService.deleteUser(userDeleteReqDto)
                .then(Mono.just(ResponseEntity.ok("User deleted successfully")));
    }
}
