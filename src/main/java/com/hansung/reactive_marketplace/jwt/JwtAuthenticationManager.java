package com.hansung.reactive_marketplace.jwt;

import com.hansung.reactive_marketplace.util.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
                .filter(auth -> !jwtUtils.isExpired(authentication.getCredentials().toString())) // 유효기간이 지났는지 검증
                .switchIfEmpty(Mono.error(new BadCredentialsException("JWT token is expired or invalid"))) // 지났으면 error 발생
                .map(auth -> { // 검증 통과하면 securityContext에 해당 인증 객체 저장
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    securityContext.setAuthentication(auth);
                    ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));
                    return auth;
                });
    }
}