package com.hansung.reactive_marketplace.jwt;

import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationManager(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .cast(JwtToken.class) // authentication 객체를 포함한 jwtToken으로 변환
                .filter(jwtToken -> !jwtUtils.isExpired(jwtToken.getToken())) // 유효기간이 지났는지 검증
                .map(jwtToken -> jwtToken.withAuthenticated(true)) // 통과하면 인증된 authentication 객체와 함께 토큰 반환
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.UNAUTHORIZED))); // 지났으면 error 발생
    }
}