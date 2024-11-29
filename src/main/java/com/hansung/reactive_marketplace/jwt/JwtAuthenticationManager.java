package com.hansung.reactive_marketplace.jwt;

import com.hansung.reactive_marketplace.service.UserService;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtils jwtUtils;

    private final UserService userService;

    public JwtAuthenticationManager(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .filter(auth -> jwtUtils.isExpired(authentication.getCredentials().toString()));
    }
}