package com.phorest.client.infrastructure.controller;

import com.phorest.client.application.dto.ClientResponse;
import com.phorest.client.application.dto.ClientUpdateRequest;
import com.phorest.client.application.service.ClientCommandService;
import com.phorest.client.application.service.ClientReadService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ClientErrorHandler.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientReadService readService;

    @MockBean
    private ClientCommandService commandService;

    @Test
    void getClientsReturnsPagedResult() throws Exception {
        Page<ClientResponse> page = new PageImpl<>(List.of(
                new ClientResponse("client-1", "Jane", "Doe", "jane@example.com", "123", "Female")
        ), PageRequest.of(0, 20), 1);
        when(readService.getClients(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/clients").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("client-1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getClientReturns404WhenMissing() throws Exception {
        when(readService.getClient("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/clients/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClientReturnsUpdatedPayload() throws Exception {
        ClientResponse response = new ClientResponse("client-1", "Jane", "Doe", "jane.new@example.com", "123", "Female");
        when(commandService.updateClient(eq("client-1"), any())).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/v1/clients/client-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "email": "jane.new@example.com",
                                  "phone": "1234567890",
                                  "gender": "Female"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.new@example.com"));

        ArgumentCaptor<ClientUpdateRequest> captor = ArgumentCaptor.forClass(ClientUpdateRequest.class);
        verify(commandService).updateClient(eq("client-1"), captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("jane.new@example.com");
    }

    @Test
    void banClientReturnsNoContentWhenSuccess() throws Exception {
        when(commandService.banClient("client-1")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/clients/client-1"))
                .andExpect(status().isNoContent());
    }
}
