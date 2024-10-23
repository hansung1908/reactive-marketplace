package com.hansung.reactive_marketplace.security;

import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class CustomLogoutHandler extends RedirectServerLogoutSuccessHandler{

    public CustomLogoutHandler() {
        super.setLogoutSuccessUrl(URI.create("/")); // 리다이렉트할 URL 설정
    }
}
