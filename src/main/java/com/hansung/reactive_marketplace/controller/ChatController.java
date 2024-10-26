package com.hansung.reactive_marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @GetMapping("/chat/chatForm")
    public String moveChat() {
        return "chat/chatForm";
    }
}
