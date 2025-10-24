package com.phorest.booking.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-should-be-32-bytes-long!!!!";

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRET, 3600);

    @Test
    void validatesGeneratedToken() {
        String token = tokenProvider.generateToken("cristian");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();

        Authentication authentication = tokenProvider.getAuthentication(token);
        assertThat(authentication.getName()).isEqualTo("cristian");
        assertThat(authentication.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertThat(tokenProvider.validateToken("invalid")).isFalse();
    }
}
