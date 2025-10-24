package com.phorest.client.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phorest.client.security.JwtTokenProvider;
import com.phorest.client.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-should-be-32-bytes-long!!!!",
        "security.jwt.expiration=3600",
        "security.user.username=cristian",
        "security.user.password=phorest2025"
})
class AuthControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnTokenWhenCredentialsValid() throws Exception {
        String payload = OBJECT_MAPPER.writeValueAsString(new AuthController.AuthRequest("cristian", "phorest2025"));

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsInvalid() throws Exception {
        String payload = OBJECT_MAPPER.writeValueAsString(new AuthController.AuthRequest("cristian", "wrong"));

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }
}
