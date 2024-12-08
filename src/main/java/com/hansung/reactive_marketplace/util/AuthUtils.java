package com.hansung.reactive_marketplace.util;

import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.security.CustomUserDetail;
import org.springframework.security.core.Authentication;

public class AuthUtils {

    public static User getAuthenticationUser(Authentication authentication) {
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        return userDetail.getUser();
    }
}
