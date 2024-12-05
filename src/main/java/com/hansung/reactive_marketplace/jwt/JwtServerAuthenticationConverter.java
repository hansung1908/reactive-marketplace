package com.hansung.reactive_marketplace.jwt;

import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER = "Bearer ";

    private final JwtUtils jwtUtils;

    private final CustomReactiveUserDetailService customReactiveUserDetailService;

    public JwtServerAuthenticationConverter(JwtUtils jwtUtils, CustomReactiveUserDetailService customReactiveUserDetailService) {
        this.jwtUtils = jwtUtils;
        this.customReactiveUserDetailService = customReactiveUserDetailService;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)) // header에서 authorization 추출
                .filter(header -> header.startsWith(BEARER)) // Bearer로 시작하는지 검증
                .map(header -> header.substring(BEARER.length())) // 있다면 이를 제거한 토큰 반환
                .flatMap(token -> {
                    String username = jwtUtils.extractUsername(token);
                    return customReactiveUserDetailService.findCustomUserDetailByUsername(username) // CustomUserDetail 반환
                            .map(userDetails -> new JwtToken(token, userDetails)); // JwtToken 생성
                });
    }
}