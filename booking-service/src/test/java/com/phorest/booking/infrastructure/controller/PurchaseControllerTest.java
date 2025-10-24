package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.PurchaseResponse;
import com.phorest.booking.application.service.PurchaseCommandService;
import com.phorest.booking.application.service.PurchaseReadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BookingErrorHandler.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseReadService readService;

    @MockBean
    private PurchaseCommandService commandService;

    @Test
    void listsPurchases() throws Exception {
        when(readService.getPurchases(any())).thenReturn(new PageImpl<>(List.of(
                new PurchaseResponse("purchase-1", "appointment-1", "client-1", "Shampoo", BigDecimal.TEN, 10, Instant.parse("2024-01-01T10:00:00Z"))
        ), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("purchase-1"));
    }

    @Test
    void updatesPurchase() throws Exception {
        when(commandService.updatePurchase(any(), any())).thenReturn(Optional.of(
                new PurchaseResponse("purchase-1", "appointment-1", "client-1", "Shampoo", BigDecimal.TEN, 10, Instant.parse("2024-01-01T10:00:00Z"))
        ));

        mockMvc.perform(put("/api/v1/purchases/purchase-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentId": "appointment-1",
                                  "name": "Shampoo",
                                  "price": 10,
                                  "loyaltyPoints": 10,
                                  "performedAt": "2024-01-01T10:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("purchase-1"));
    }

    @Test
    void deletesPurchase() throws Exception {
        when(commandService.deletePurchase("purchase-1")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/purchases/purchase-1"))
                .andExpect(status().isNoContent());
    }
}
