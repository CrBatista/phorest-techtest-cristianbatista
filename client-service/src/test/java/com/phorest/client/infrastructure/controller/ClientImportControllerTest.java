package com.phorest.client.infrastructure.controller;

import com.phorest.client.application.dto.ClientImportSummary;
import com.phorest.client.application.service.ClientImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientImportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ClientErrorHandler.class)
class ClientImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientImportService importService;

    @Test
    void shouldReturnSummaryFromService() throws Exception {
        when(importService.importClients(any())).thenReturn(new ClientImportSummary(2, 2, 0, 0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "clients.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "id,first_name\nclient-1,Jane".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/import/clients").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(2))
                .andExpect(jsonPath("$.created").value(2));

        verify(importService).importClients(any());
    }
}
