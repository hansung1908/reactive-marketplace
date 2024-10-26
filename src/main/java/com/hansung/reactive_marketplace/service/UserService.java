package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new CustomUserDetail(user));
    }

    public Mono<User> saveUser(UserSaveReqDto userSaveReqDto) {
        User user = new User.Builder()
                .username(userSaveReqDto.getUsername())
                .nickname(userSaveReqDto.getNickname())
                .password(bCryptPasswordEncoder.encode(userSaveReqDto.getPassword()))
                .email(userSaveReqDto.getEmail())
                .build();

        return userRepository.save(user);
    }

    public Mono<User> updateUser(UserUpdateReqDto userUpdateReqDto) {
        Query query = new Query(Criteria.where("id").is(userUpdateReqDto.getId())); // 유저 정보 찾기
        return reactiveMongoTemplate.findOne(query, User.class)
                .flatMap(user -> {
                    Update update = new Update()
                            .set("nickname", userUpdateReqDto.getNickname())
                            .set("email", userUpdateReqDto.getEmail())
                            .set("password", userUpdateReqDto.getPassword() == null || userUpdateReqDto.getPassword().equals("")
                                    ? user.getPassword() // null이면 기존 비밀번호
                                    : bCryptPasswordEncoder.encode(userUpdateReqDto.getPassword())); // 새로운 값이 있으면 인코딩 후 저장

                    return reactiveMongoTemplate.updateFirst(query, update, User.class) // 업데이트 실행
                            .flatMap(result -> Mono.just(user)); // 업데이트 후 유저 정보 반환
                });
    }

    public Mono<Void> deleteUser(UserDeleteReqDto userDeleteReqDto) {
        return userRepository.deleteById(userDeleteReqDto.getId());
    }
}
