package com.hansung.reactive_marketplace.config;

import com.hansung.reactive_marketplace.jwt.JwtAuthenticationManager;
import com.hansung.reactive_marketplace.jwt.JwtServerAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationManager authenticationManager;

    private final JwtServerAuthenticationConverter authenticationConverter;

    public SecurityConfig(JwtAuthenticationManager authenticationManager, JwtServerAuthenticationConverter authenticationConverter) {
        this.authenticationManager = authenticationManager;
        this.authenticationConverter = authenticationConverter;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter(JwtAuthenticationManager authenticationManager,
                                                           JwtServerAuthenticationConverter authenticationConverter) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);
        return authenticationWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrfSpec -> csrfSpec.disable())
                .formLogin(formLoginSpec -> formLoginSpec.disable())
                .httpBasic(httpBasicSpec -> httpBasicSpec.disable())

                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .anyExchange().permitAll())

                .addFilterAt(authenticationWebFilter(authenticationManager, authenticationConverter),
                        SecurityWebFiltersOrder.AUTHENTICATION) // jwt 로그인 검증 필터 추가

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) ; // session을 STATELESS 상태로 변경

        return http.build();
    }
}
