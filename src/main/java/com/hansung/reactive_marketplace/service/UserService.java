package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new CustomUserDetail(user));
    }
}
