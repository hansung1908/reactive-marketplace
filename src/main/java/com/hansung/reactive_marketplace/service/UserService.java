package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface UserService {

    // 사용자 저장
    Mono<User> saveUser(UserSaveReqDto userSaveReqDto, FilePart image);

    // 사용자명으로 사용자 조회
    Mono<User> findUserByUsername(String username);

    // 사용자 ID로 사용자 조회
    Mono<User> findUserById(String userId);

    // 사용자 정보 수정
    Mono<Void> updateUser(UserUpdateReqDto userUpdateReqDto, FilePart image);

    // 사용자 삭제
    Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto);
}
