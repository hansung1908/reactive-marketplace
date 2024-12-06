package com.hansung.reactive_marketplace.config;

import com.hansung.reactive_marketplace.jwt.CustomServerAuthenticationSuccessHandler;
import com.hansung.reactive_marketplace.jwt.JwtAuthenticationManager;
import com.hansung.reactive_marketplace.jwt.JwtServerAuthenticationConverter;
import com.hansung.reactive_marketplace.security.CustomLogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final CustomLogoutHandler customLogoutHandler;

    private final JwtAuthenticationManager jwtAuthenticationManager;

    private final JwtServerAuthenticationConverter jwtServerAuthenticationConverter;

    private final CustomServerAuthenticationSuccessHandler customServerAuthenticationSuccessHandler;

    public SecurityConfig(CustomLogoutHandler customLogoutHandler, JwtAuthenticationManager jwtAuthenticationManager, JwtServerAuthenticationConverter jwtServerAuthenticationConverter, CustomServerAuthenticationSuccessHandler customServerAuthenticationSuccessHandler) {
        this.customLogoutHandler = customLogoutHandler;
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.jwtServerAuthenticationConverter = jwtServerAuthenticationConverter;
        this.customServerAuthenticationSuccessHandler = customServerAuthenticationSuccessHandler;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter(JwtAuthenticationManager jwtAuthenticationManager,
                                                           JwtServerAuthenticationConverter jwtServerAuthenticationConverter,
                                                           CustomServerAuthenticationSuccessHandler customServerAuthenticationSuccessHandler) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter);
        authenticationWebFilter.setAuthenticationSuccessHandler(customServerAuthenticationSuccessHandler);
        return authenticationWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrfSpec -> csrfSpec.disable())
                .anonymous(Customizer.withDefaults()) // thymeleaf spring security에서 anonymous 사용을 위한 활성화
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/", "/js/**", "image/**", "/user/saveForm", "/user/loginForm").permitAll()
                        .pathMatchers("/products", "/auth/**", "/user/save").permitAll()
                        .anyExchange().authenticated())

                .formLogin(formLoginSpec -> formLoginSpec.disable())
                .httpBasic(httpBasicSpec -> httpBasicSpec.disable())

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // session을 STATELESS 상태로 변경

                .addFilterAt(authenticationWebFilter(jwtAuthenticationManager, jwtServerAuthenticationConverter, customServerAuthenticationSuccessHandler),
                        SecurityWebFiltersOrder.AUTHENTICATION) // jwt 로그인 검증 필터 추가

                .logout(logoutSpec -> logoutSpec
                        .logoutSuccessHandler(customLogoutHandler));

        return http.build();
    }
}
