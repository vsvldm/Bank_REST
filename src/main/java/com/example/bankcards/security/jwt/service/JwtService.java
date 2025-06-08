package com.example.bankcards.security.jwt.service;

import com.example.bankcards.security.jwt.dto.JwtRequest;
import com.example.bankcards.security.jwt.dto.JwtResponse;

public interface JwtService {
    JwtResponse createToken(JwtRequest jwtRequest);
}
