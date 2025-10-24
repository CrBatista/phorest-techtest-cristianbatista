package com.phorest.client.infrastructure.controller;

import com.phorest.client.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final String configuredUsername;
    private final String configuredPassword;

    public AuthController(JwtTokenProvider jwtTokenProvider,
                          @Value("${security.user.username}") String configuredUsername,
                          @Value("${security.user.password}") String configuredPassword) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
    }

    @PostMapping("/token")
    public ResponseEntity<AuthResponse> generateToken(@RequestBody AuthRequest request) {
        if (configuredUsername.equals(request.username()) && configuredPassword.equals(request.password())) {
            String token = jwtTokenProvider.generateToken(configuredUsername);
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public record AuthRequest(String username, String password) {}

    public record AuthResponse(String token) {}
}
