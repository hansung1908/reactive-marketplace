package com.hansung.reactive_marketplace.util;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.springframework.security.core.Authentication;

// 사용자 관련 메소드를 모아놓은 유틸리티 클래스
public class AuthUtils {

    public static User getAuthenticationUser(Authentication authentication) {
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        return userDetail.getUser();
    }
}
