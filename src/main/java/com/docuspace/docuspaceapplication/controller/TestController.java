package com.docuspace.docuspaceapplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/secure")
    public String secureEndpoint() {
        return "You have accessed a protected endpoint";
    }
}