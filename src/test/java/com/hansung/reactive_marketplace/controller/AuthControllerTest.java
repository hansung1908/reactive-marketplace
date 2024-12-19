package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.jwt.JwtAuthenticationManager;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import com.hansung.reactive_marketplace.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // 전체 컨텍스트 로드를 위한 어노테이션
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;  // 테스트 클라이언트 (WebFlux 기반의 테스트용 클라이언트)

    @MockBean
    private CustomReactiveUserDetailService customReactiveUserDetailService;  // 사용자 상세 정보 서비스 모킹

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;  // BCryptPasswordEncoder 모킹

    @MockBean
    private JwtUtils jwtUtils;  // JWT 생성 유틸리티 모킹

    @MockBean
    private JwtAuthenticationManager authenticationManager;

    private LoginReqDto validLoginReqDto;  // 유효한 로그인 요청 DTO
    private LoginReqDto invalidLoginReqDto;  // 잘못된 로그인 요청 DTO

    @BeforeEach
    void setUp() {
        // 컨트롤러 바인딩 설정
        webTestClient = WebTestClient.bindToController(new AuthController(bCryptPasswordEncoder, customReactiveUserDetailService, jwtUtils)).build();

        // 테스트 데이터 설정
        validLoginReqDto = new LoginReqDto("validUsername", "validPassword");  // 유효한 로그인 요청
        invalidLoginReqDto = new LoginReqDto("invalidUsername", "invalidPassword");  // 잘못된 로그인 요청
    }

    @Test
    void testLoginSuccess() {
        // 로그인에 성공할 유저 데이터 준비
        User user = new User.Builder()
                .username("validUsername")
                .nickname("TestUser")
                .password("validPassword")
                .email("testuser@example.com")
                .build();

        // CustomUserDetail 객체 생성 (User 데이터로부터)
        CustomUserDetail userDetail = new CustomUserDetail(user);

        // 모의 토큰 생성
        String accessToken = "mockToken";

        // 모의 응답 생성
        ResponseEntity<String> mockResponse = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "JWT_TOKEN=mockToken")
                .body("Login successful");

        // Mocking 서비스 동작
        when(customReactiveUserDetailService.findCustomUserDetailByUsername(validLoginReqDto.username())).thenReturn(Mono.just(userDetail));
        when(bCryptPasswordEncoder.matches(validLoginReqDto.password(), userDetail.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(any(CustomUserDetail.class))).thenReturn(Mono.just(accessToken));
        when(jwtUtils.createLoginResponse(accessToken)).thenReturn(mockResponse);

        // 로그인 테스트 수행
        webTestClient.post()
                .uri("/auth/login")
                .cookie("JWT_TOKEN", accessToken)
                .bodyValue(validLoginReqDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, "JWT_TOKEN=mockToken")
                .expectBody(String.class).isEqualTo("Login successful");
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // 로그인에 실패할 유저 데이터 준비
        User user = new User.Builder()
                .username("invalidUsername")
                .nickname("Test User")
                .password("wrongPassword")
                .email("testuser@example.com")
                .build();

        // CustomUserDetail 객체 생성
        CustomUserDetail userDetail = new CustomUserDetail(user);

        // 잘못된 비밀번호 비교
        when(customReactiveUserDetailService.findCustomUserDetailByUsername(invalidLoginReqDto.username())).thenReturn(Mono.just(userDetail));  // 사용자 찾기
        when(bCryptPasswordEncoder.matches(invalidLoginReqDto.password(), userDetail.getPassword())).thenReturn(false);  // 비밀번호 불일치 처리

        // 로그인 실패 테스트 수행 (잘못된 자격 증명)
        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(invalidLoginReqDto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginFailureUserNotFound() {
        // 사용자 데이터가 존재하지 않을 경우 테스트
        when(customReactiveUserDetailService.findCustomUserDetailByUsername(invalidLoginReqDto.username())).thenReturn(Mono.empty());  // 사용자 없음

        // 로그인 실패 테스트 수행 (사용자 없음)
        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(invalidLoginReqDto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void logoutTest() {
        // 로그아웃 테스트 수행
        webTestClient.post()
                .uri("/auth/logout")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Set-Cookie",
                        "JWT_TOKEN=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly")
                .expectBody(String.class).isEqualTo("Logout successful");
    }
}
