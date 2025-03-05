package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.jwt.JwtTokenManager;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import org.springframework.http.HttpHeaders;
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

    private final JwtTokenManager jwtTokenManager;

    public AuthController(BCryptPasswordEncoder bCryptPasswordEncoder, CustomReactiveUserDetailService customReactiveUserDetailService, JwtTokenManager jwtTokenManager) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.customReactiveUserDetailService = customReactiveUserDetailService;
        this.jwtTokenManager = jwtTokenManager;
    }

    @PostMapping("/auth/login")
    public Mono<ResponseEntity<String>> login(@RequestBody LoginReqDto loginReqDto) {
        return customReactiveUserDetailService.findCustomUserDetailByUsername(loginReqDto.username())
                .filter(user -> bCryptPasswordEncoder.matches(loginReqDto.password(), user.getPassword()))
                .flatMap(userDetail -> jwtTokenManager.createToken(userDetail))
                .map(token -> createLoginResponse(token))
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.INVALID_CREDENTIALS)));
    }

    private ResponseEntity<String> createLoginResponse(String token) {
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                .httpOnly(true)
//                .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Login successful");
    }

    @PostMapping("/auth/logout")
    public Mono<ResponseEntity<String>> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", "")
                .maxAge(0) // 유효시간을 0으로 설정하여 시간 종료로 쿠키 삭제
                .httpOnly(true)
//                .secure(true)
                .path("/")
                .build();

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful"));
    }
}
