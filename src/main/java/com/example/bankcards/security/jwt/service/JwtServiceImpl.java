package com.example.bankcards.security.jwt.service;

import com.example.bankcards.exception.exception.UnauthorizedException;
import com.example.bankcards.security.jwt.dto.JwtRequest;
import com.example.bankcards.security.jwt.dto.JwtResponse;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public JwtResponse createToken(JwtRequest jwtRequest) {
        try {
          authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                  jwtRequest.getUsername(),
                  jwtRequest.getPassword()));

          UserDetails userDetails = userService.loadUserByUsername(jwtRequest.getUsername());
          String token = jwtTokenUtils.generateToken(userDetails);

          return new JwtResponse(token);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid username or password.");
        }
    }
}
