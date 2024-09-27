package com.hansung.reactive_marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/product/saveForm")
    public String saveForm() {
        return "product/saveForm";
    }
}
