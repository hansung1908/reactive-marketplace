package com.hansung.reactive_marketplace.jwt;

import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtUtils jwtUtils;

    private final CustomReactiveUserDetailService customReactiveUserDetailService;

    public JwtServerAuthenticationConverter(JwtUtils jwtUtils, CustomReactiveUserDetailService customReactiveUserDetailService) {
        this.jwtUtils = jwtUtils;
        this.customReactiveUserDetailService = customReactiveUserDetailService;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst("JWT_TOKEN")) // 쿠키에서 jwt token 확인
                .map(cookie -> cookie.getValue()) // 쿠키 값 부분 가져오기
                .flatMap(token -> {
                    String username = jwtUtils.extractUsername(token);
                    return customReactiveUserDetailService.findCustomUserDetailByUsername(username) // CustomUserDetail 반환
                            .map(userDetails -> new JwtToken(token, userDetails)); // JwtToken 생성
                });
    }
}