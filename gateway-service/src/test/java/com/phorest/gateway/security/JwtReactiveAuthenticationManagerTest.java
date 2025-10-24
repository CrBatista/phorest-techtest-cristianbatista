package com.phorest.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.test.StepVerifier;

class JwtReactiveAuthenticationManagerTest {

    private static final String SECRET = "test-secret-should-be-32-bytes-long!!!!";

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRET, 3600);
    private final JwtReactiveAuthenticationManager authenticationManager = new JwtReactiveAuthenticationManager(tokenProvider);

    @Test
    void authenticatesValidToken() {
        String token = tokenProvider.generateToken("cristian");
        Authentication request = new UsernamePasswordAuthenticationToken(token, token);

        StepVerifier.create(authenticationManager.authenticate(request))
                .expectNextMatches(auth -> "cristian".equals(auth.getName()))
                .verifyComplete();
    }

    @Test
    void rejectsInvalidToken() {
        Authentication request = new UsernamePasswordAuthenticationToken("invalid", "invalid");

        StepVerifier.create(authenticationManager.authenticate(request))
                .verifyComplete();
    }
}
