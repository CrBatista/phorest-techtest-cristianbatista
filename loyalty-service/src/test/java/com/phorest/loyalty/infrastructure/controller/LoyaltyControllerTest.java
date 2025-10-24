package com.phorest.loyalty.infrastructure.controller;

import com.phorest.loyalty.application.dto.TopClientResponse;
import com.phorest.loyalty.application.service.TopClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoyaltyController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopClientService topClientService;

    @Test
    void returnsTopClients() throws Exception {
        when(topClientService.topClients(anyInt(), any(), any())).thenReturn(List.of(
                new TopClientResponse("client-1", 100)
        ));

        mockMvc.perform(get("/api/v1/loyalty/top-clients")
                        .param("limit", "5")
                        .param("from", LocalDate.of(2024, 1, 1).toString())
                        .param("to", LocalDate.of(2024, 12, 31).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value("client-1"));
    }
}
