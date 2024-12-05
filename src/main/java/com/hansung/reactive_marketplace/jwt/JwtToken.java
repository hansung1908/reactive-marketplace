package com.hansung.reactive_marketplace.jwt;

import com.hansung.reactive_marketplace.security.CustomUserDetail;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

@Getter
public class JwtToken extends AbstractAuthenticationToken {

    private final String token;
    private final CustomUserDetail principal;

    // 생성자 주입으로 token과 함께 userDetail도 가져옴
    public JwtToken(String token, CustomUserDetail principal) {
        super(principal.getAuthorities());
        this.token = token;
        this.principal = principal;
    }

    // authencation 인증 처리와 함께 해당 타입으로 반환
    public Authentication withAuthenticated(boolean isAuthenticated) {
        JwtToken cloned = new JwtToken(token, principal);
        cloned.setAuthenticated(isAuthenticated);
        return cloned;
    }


    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
