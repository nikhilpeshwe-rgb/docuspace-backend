package com.docuspace.docuspaceapplication.controller;


import com.docuspace.docuspaceapplication.dto.LoginRequest;
import com.docuspace.docuspaceapplication.dto.LoginResponse;
import com.docuspace.docuspaceapplication.dto.RegisterRequest;
import com.docuspace.docuspaceapplication.service.Authservice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final Authservice authservice;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authservice.register(request));
    }

    @GetMapping("/getVal")
    public ResponseEntity<String> getVal()
    {
        return  ResponseEntity.ok("Hello1");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authservice.login(request));
    }
}
