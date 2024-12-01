package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final CustomReactiveUserDetailService customReactiveUserDetailService;

    private final JwtUtils jwtUtils;

    public AuthController(BCryptPasswordEncoder bCryptPasswordEncoder, CustomReactiveUserDetailService customReactiveUserDetailService, JwtUtils jwtUtils) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.customReactiveUserDetailService = customReactiveUserDetailService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/auth/login")
    Mono<String> login(@RequestBody LoginReqDto loginReqDto) {
        return customReactiveUserDetailService.findByUsername(loginReqDto.username())
                .filter(user -> bCryptPasswordEncoder.matches(loginReqDto.password(), user.getPassword()))
                .map(user -> "Bearer " + jwtUtils.createJwt(loginReqDto.username(), user.getAuthorities().toString()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("invalid username or password")));
    }
}
