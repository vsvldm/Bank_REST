package com.example.bankcards.security.jwt.controller;

import com.example.bankcards.security.jwt.dto.JwtRequest;
import com.example.bankcards.security.jwt.dto.JwtResponse;
import com.example.bankcards.security.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;

    @PostMapping
    public JwtResponse createToken(@RequestBody JwtRequest jwtRequest) {
        return jwtService.createToken(jwtRequest);
    }
}
