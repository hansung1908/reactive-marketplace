package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@WebFluxTest(AuthController.class)  // AuthController만 테스트하는 WebFluxTest 설정
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;  // 테스트 클라이언트 (WebFlux 기반의 테스트용 클라이언트)

    @MockBean
    private CustomReactiveUserDetailService customReactiveUserDetailService;  // 사용자 상세 정보 서비스 모킹

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;  // BCryptPasswordEncoder 모킹

    @MockBean
    private JwtUtils jwtUtils;  // JWT 생성 유틸리티 모킹

    private LoginReqDto validLoginReqDto;  // 유효한 로그인 요청 DTO
    private LoginReqDto invalidLoginReqDto;  // 잘못된 로그인 요청 DTO

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        validLoginReqDto = new LoginReqDto("validUsername", "validPassword");  // 유효한 로그인 요청
        invalidLoginReqDto = new LoginReqDto("invalidUsername", "invalidPassword");  // 잘못된 로그인 요청
    }

    @Test
    @WithMockUser(username = "validUsername", roles = "USER")
    void testLoginSuccess() {
        // 로그인에 성공할 유저 데이터 준비
        User user = new User.Builder()
                .username("validUsername")
                .nickname("Test User")
                .password("validPassword")
                .email("testuser@example.com")
                .build();

        // CustomUserDetail 객체 생성 (User 데이터로부터)
        CustomUserDetail userDetail = new CustomUserDetail(user);

        // 암호화된 비밀번호
        String encodedPassword = bCryptPasswordEncoder.encode(validLoginReqDto.password());  // 예시 암호화된 비밀번호

        // Mocking 서비스 동작
        when(customReactiveUserDetailService.findByUsername(validLoginReqDto.username())).thenReturn(Mono.just(userDetail));  // 사용자 찾기
        when(bCryptPasswordEncoder.matches(validLoginReqDto.password(), encodedPassword)).thenReturn(true);  // 비밀번호 비교
        when(jwtUtils.createJwt(validLoginReqDto.username(), "USER")).thenReturn("mocked-jwt-token");  // JWT 생성

        // 로그인 테스트 수행
        webTestClient.mutateWith(csrf())  // CSRF 토큰을 자동으로 포함
                .post()  // POST 요청
                .uri("/auth/login")  // URI 설정
                .bodyValue(validLoginReqDto)  // 요청 본문에 로그인 데이터 설정
                .exchange()  // 요청 전송
                .expectStatus().isOk()  // 상태 코드 200 OK 확인
                .expectBody()
                .jsonPath("$.token").isEqualTo("mocked-jwt-token");  // 응답 JSON에서 token 필드가 "mocked-jwt-token"인지 확인
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // 로그인에 실패할 유저 데이터 준비
        User user = new User.Builder()
                .username("invalidUsername")
                .nickname("Test User")
                .password("invalidPassword")
                .email("testuser@example.com")
                .build();

        // CustomUserDetail 객체 생성
        CustomUserDetail userDetail = new CustomUserDetail(user);

        // 잘못된 비밀번호 비교
        when(customReactiveUserDetailService.findByUsername(invalidLoginReqDto.username())).thenReturn(Mono.just(userDetail));  // 사용자 찾기
        when(bCryptPasswordEncoder.matches(invalidLoginReqDto.password(), user.getPassword())).thenReturn(false);  // 비밀번호 불일치 처리

        // 로그인 실패 테스트 수행 (잘못된 자격 증명)
        webTestClient.mutateWith(csrf())  // CSRF 토큰을 자동으로 포함
                .post()  // POST 요청
                .uri("/auth/login")  // URI 설정
                .bodyValue(invalidLoginReqDto)  // 요청 본문에 잘못된 로그인 데이터 설정
                .exchange()  // 요청 전송
                .expectStatus().isUnauthorized();  // 상태 코드 401 Unauthorized 확인
    }

    @Test
    void testLoginFailureUserNotFound() {
        // 사용자 데이터가 존재하지 않을 경우 테스트
        when(customReactiveUserDetailService.findByUsername(invalidLoginReqDto.username())).thenReturn(Mono.empty());  // 사용자 없음

        // 로그인 실패 테스트 수행 (사용자 없음)
        webTestClient.mutateWith(csrf())  // CSRF 토큰을 자동으로 포함
                .post()  // POST 요청
                .uri("/auth/login")  // URI 설정
                .bodyValue(invalidLoginReqDto)  // 요청 본문에 잘못된 로그인 데이터 설정
                .exchange()  // 요청 전송
                .expectStatus().isUnauthorized();  // 상태 코드 401 Unauthorized 확인
    }
}
