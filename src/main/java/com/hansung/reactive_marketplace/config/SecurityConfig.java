package com.hansung.reactive_marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrfSpec -> csrfSpec.disable())
                .anonymous(Customizer.withDefaults()) // thymeleaf spring security에서 anonymous 사용을 위한 활성화
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/", "/js/**", "/user/**").permitAll()
                        .pathMatchers("/products", "/login").permitAll()
                        .anyExchange().authenticated())

                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
