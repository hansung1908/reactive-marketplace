package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.service.ImageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class UserController {

    private final ImageService imageService;

    public UserController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/user/saveForm")
    public String saveForm() {
        return "user/saveForm";
    }

    @GetMapping("/user/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

    @GetMapping("/user/profileForm/{id}")
    public Mono<Rendering> profileForm(@PathVariable("id") String id) {
        return imageService.findProfileImageById(id)
                .map(profile -> Rendering.view("user/profileForm")
                        .modelAttribute("profile", profile)
                        .build());
    }
}
