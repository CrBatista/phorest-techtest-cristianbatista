package com.phorest.loyalty.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-should-be-32-bytes-long!!!!";

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRET, 3600);

    @Test
    void generatesAndValidatesToken() {
        String token = tokenProvider.generateToken("cristian");

        assertThat(tokenProvider.validateToken(token)).isTrue();
        Authentication authentication = tokenProvider.getAuthentication(token);
        assertThat(authentication.getName()).isEqualTo("cristian");
    }

    @Test
    void invalidTokenIsRejected() {
        assertThat(tokenProvider.validateToken("invalid")).isFalse();
    }
}
