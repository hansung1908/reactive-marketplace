package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.jwt.JwtCategory;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
    Mono<ResponseEntity<String>> login(@RequestBody LoginReqDto loginReqDto) {
        return customReactiveUserDetailService.findByUsername(loginReqDto.username())
                .filter(user -> bCryptPasswordEncoder.matches(loginReqDto.password(), user.getPassword()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("invalid username or password")))
                .map(user -> {
                    String username = loginReqDto.username();
                    String role = user.getAuthorities().toString();

                    // 다중 (access / refresh) 토큰 생성
                    String accessToken = "Bearer " + jwtUtils.createJwt(JwtCategory.ACCESS, username, role);
                    String refreshToken = jwtUtils.createJwt(JwtCategory.REFRESH, username, role);

                    // access 토큰을 header에 추가
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.AUTHORIZATION, accessToken);

                    // refresh 토큰을 cookie에 추가
                    ResponseCookie cookie = ResponseCookie.from("jwt", refreshToken)
                            .httpOnly(true) // JavaScript를 통한 접근을 막아 XSS(Cross-Site Scripting) 공격 방지
                            .secure(true) // HTTPS를 통해서만 쿠키 전송을 허용하여 보안 강화
                            .path("/") // 쿠키가 유효한 경로를 루트(/)로 설정하여 전체 애플리케이션에서 사용 가능
                            .maxAge(24 * 60 * 60) // 쿠키의 최대 유효 기간을 24시간(초 단위)으로 설정
                            .build();  // ResponseCookie 객체 생성
                    headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body("Login successful");
                });
    }
}
