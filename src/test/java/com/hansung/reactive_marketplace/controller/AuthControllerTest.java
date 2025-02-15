package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.LoginReqDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.jwt.JwtAuthenticationManager;
import com.hansung.reactive_marketplace.jwt.JwtTokenManager;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // 전체 컨텍스트 로드를 위한 어노테이션
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;  // 테스트 클라이언트 (WebFlux 기반의 테스트용 클라이언트)

    @Autowired
    private AuthController authController;

    @MockBean
    private CustomReactiveUserDetailService customReactiveUserDetailService;  // 사용자 상세 정보 서비스 모킹

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;  // BCryptPasswordEncoder 모킹

    @MockBean
    private JwtTokenManager jwtTokenManager;  // JWT 생성 유틸리티 모킹

    @MockBean
    private JwtAuthenticationManager authenticationManager;

    private static final String MOCK_TOKEN = "mockToken";
    private LoginReqDto validLoginReqDto;
    private LoginReqDto invalidLoginReqDto;
    private User user;
    private CustomUserDetail userDetail;

    @BeforeEach
    void setUp() {
        // 컨트롤러 바인딩 설정
        webTestClient = WebTestClient.bindToController(new AuthController(bCryptPasswordEncoder, customReactiveUserDetailService, jwtTokenManager)).build();

        // 테스트 데이터 설정
        validLoginReqDto = new LoginReqDto("validUsername", "validPassword");  // 유효한 로그인 요청
        invalidLoginReqDto = new LoginReqDto("invalidUsername", "invalidPassword");  // 잘못된 로그인 요청

        // 로그인에 성공할 유저 데이터 준비
        user = new User.Builder()
                .username("validUsername")
                .nickname("TestUser")
                .password("validPassword")
                .email("testuser@example.com")
                .build();

        // CustomUserDetail 객체 생성 (User 데이터로부터)
        userDetail = new CustomUserDetail(user);
    }

    @Test
    void testLoginSuccess() {
        // Mocking 서비스 동작
        when(customReactiveUserDetailService.findCustomUserDetailByUsername(validLoginReqDto.username())).thenReturn(Mono.just(userDetail));
        when(bCryptPasswordEncoder.matches(validLoginReqDto.password(), userDetail.getPassword())).thenReturn(true);
        when(jwtTokenManager.createToken(any(CustomUserDetail.class))).thenReturn(Mono.just(MOCK_TOKEN));

        // 로그인 테스트 수행
        webTestClient.post()
                .uri("/auth/login")
                .cookie("JWT_TOKEN", MOCK_TOKEN)
                .bodyValue(validLoginReqDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, "JWT_TOKEN=mockToken; Path=/; Secure; HttpOnly; SameSite=Strict")
                .expectBody(String.class).isEqualTo("Login successful");
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // 잘못된 비밀번호 비교
        when(customReactiveUserDetailService.findCustomUserDetailByUsername(invalidLoginReqDto.username())).thenReturn(Mono.just(userDetail));  // 사용자 찾기
        when(bCryptPasswordEncoder.matches(invalidLoginReqDto.password(), userDetail.getPassword())).thenReturn(false);  // 비밀번호 불일치 처리

        // 로그인 실패 테스트 수행 (잘못된 자격 증명)
        StepVerifier.create(authController.login(invalidLoginReqDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiException
                        && ((ApiException) throwable).getException() == ExceptionMessage.INVALID_CREDENTIALS)
                .verify();
    }

    @Test
    void testLoginFailureUserNotFound() {
        // 사용자 데이터가 존재하지 않을 경우 테스트
        when(customReactiveUserDetailService.findCustomUserDetailByUsername(invalidLoginReqDto.username())).thenReturn(Mono.empty());  // 사용자 없음

        // 로그인 실패 테스트 수행 (사용자 없음)
        StepVerifier.create(authController.login(invalidLoginReqDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiException
                        && ((ApiException) throwable).getException() == ExceptionMessage.INVALID_CREDENTIALS)
                .verify();
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
