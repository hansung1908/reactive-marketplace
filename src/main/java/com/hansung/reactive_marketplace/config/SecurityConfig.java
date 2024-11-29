package com.hansung.reactive_marketplace.config;

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

    public SecurityConfig(CustomLogoutHandler customLogoutHandler, JwtAuthenticationManager jwtAuthenticationManager, JwtServerAuthenticationConverter jwtServerAuthenticationConverter) {
        this.customLogoutHandler = customLogoutHandler;
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.jwtServerAuthenticationConverter = jwtServerAuthenticationConverter;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter(JwtAuthenticationManager jwtAuthenticationManager,
                                                           JwtServerAuthenticationConverter jwtServerAuthenticationConverter) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter);
        return authenticationWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrfSpec -> csrfSpec.disable())
                .anonymous(Customizer.withDefaults()) // thymeleaf spring security에서 anonymous 사용을 위한 활성화
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/", "/js/**", "image/**", "/user/saveForm").permitAll()
                        .pathMatchers("/products", "/login", "/user/save").permitAll()
                        .anyExchange().authenticated())

                .formLogin(formLoginSpec -> formLoginSpec.disable())
                .httpBasic(httpBasicSpec -> httpBasicSpec.disable())

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // session을 STATELESS 상태로 변경

                .addFilterAt(authenticationWebFilter(jwtAuthenticationManager, jwtServerAuthenticationConverter), SecurityWebFiltersOrder.AUTHENTICATION)

                .logout(logoutSpec -> logoutSpec
                        .logoutSuccessHandler(customLogoutHandler));

        return http.build();
    }
}
