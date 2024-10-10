package com.hansung.reactive_marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/user/saveForm")
    public String saveForm() {
        return "user/saveForm";
    }
}
