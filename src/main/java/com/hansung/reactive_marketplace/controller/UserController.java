package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/saveForm")
    public String saveForm() {
        return "user/saveForm";
    }

    @GetMapping("/user/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

    @GetMapping("/user/profileForm")
    public Mono<Rendering> profileForm(Authentication authentication) {
        return userService.findUserProfile(authentication)
                .map(profile -> Rendering.view("user/profileForm")
                        .modelAttribute("profile", profile)
                        .build());
    }
}
