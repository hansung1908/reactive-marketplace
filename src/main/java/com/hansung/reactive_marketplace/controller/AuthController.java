package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<String>> login(@RequestBody LoginReqDto loginReqDto) {
        return customReactiveUserDetailService.findByUsername(loginReqDto.username())
                .filter(user -> bCryptPasswordEncoder.matches(loginReqDto.password(), user.getPassword()))
                .flatMap(userDetails -> jwtUtils.generateTokens(userDetails))
                .map(tokenPair -> jwtUtils.createLoginResponse(tokenPair))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username or password")));
    }

    @PostMapping("/auth/logout")
    public Mono<ResponseEntity<String>> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", "")
                .maxAge(0) // 유효시간을 0으로 설정하여 시간 종료로 쿠키 삭제
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful"));
    }
}
