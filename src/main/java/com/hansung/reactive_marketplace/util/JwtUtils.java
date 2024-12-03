package com.hansung.reactive_marketplace.util;

import com.hansung.reactive_marketplace.jwt.JwtCategory;
import com.hansung.reactive_marketplace.security.CustomReactiveUserDetailService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// jwt token과 관련된 메소드를 모아놓은 유틸리티 클래스
@Component
public class JwtUtils {

    private static final long expiredMs = 24 * 60 * 60L;

    private final SecretKey secretKey;

    private final CustomReactiveUserDetailService customReactiveUserDetailService;

    public JwtUtils(@Value("${spring.jwt.secret}")String secret, CustomReactiveUserDetailService customReactiveUserDetailService) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.customReactiveUserDetailService = customReactiveUserDetailService;
    }

    // jwt token에서 username 값을 추출
    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token) // parser를 통해 secretKey와 token을 검증
                .getPayload() // payload 추출
                .get("username", String.class); // String returnType으로 username 추출
    }

    // jwt token에서 password 값을 추출
    public String extractPassword(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token) // parser를 통해 secretKey와 token을 검증
                .getPayload() // payload 추출
                .get("password", String.class); // String returnType으로 username 추출
    }

    // jwt token에서 role 값을 추출
    public List<String> extractRole(String token) {
        return Collections.singletonList(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload()
                .get("role", String.class)); // String returnType으로 role 추출
    }

    // token 만료 기한을 확인
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload()
                .getExpiration() // 만료 기한(exp) 추출
                .before(new Date()); // 현재 시간이 만료 기한 전인지 검증
    }

    // jwt token 생성
    public String createJwt(JwtCategory jwtCategory, String username, String role) {
        return Jwts.builder() // jwt token 빌더 생성
                .claim("category", jwtCategory)
                .claim("username", username) // username 추가
                .claim("role", role) // role 추가
                .issuedAt(new Date(System.currentTimeMillis())) // 발행 시간(iat) 설정
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // 만료 기한(exp) 설정
                .signWith(secretKey) // secretKey 암호화
                .compact(); // 문자열로 직렬화 + 토큰 실제 생성
    }

    // userDetail 생성
    public UserDetails createUserDetails(String token) {
        String username = extractUsername(token);
        return User.builder()
                .username(username)
                .authorities(createAuthorities(token))
                .password("")
                .build();
    }

    // Authority 생성
    public List<SimpleGrantedAuthority> createAuthorities(String token) {
        return extractRole(token).stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Mono<TokenPair> generateTokens(UserDetails user) {
        String username = user.getUsername();
        String role = user.getAuthorities().toString();
        String accessToken = createJwt(JwtCategory.ACCESS, username, role);
        String refreshToken = createJwt(JwtCategory.REFRESH, username, role);
        return Mono.just(new TokenPair(accessToken, refreshToken));
    }

    public ResponseEntity<String> createLoginResponse(TokenPair tokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken());

        ResponseCookie cookie = ResponseCookie.from("jwt", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofHours(24))
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body("Login successful");
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
