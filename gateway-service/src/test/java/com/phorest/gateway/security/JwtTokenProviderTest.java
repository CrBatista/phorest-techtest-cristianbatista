package com.phorest.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-should-be-32-bytes-long!!!!";

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRET, 3600);

    @Test
    void validatesGeneratedToken() {
        String token = tokenProvider.generateToken("cristian");

        assertThat(tokenProvider.validateToken(token)).isTrue();
        Authentication authentication = tokenProvider.getAuthentication(token);
        assertThat(authentication.getName()).isEqualTo("cristian");
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertThat(tokenProvider.validateToken("invalid")).isFalse();
    }
}
