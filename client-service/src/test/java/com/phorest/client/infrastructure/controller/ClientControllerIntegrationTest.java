package com.phorest.client.infrastructure.controller;

import com.phorest.client.infrastructure.repository.document.ClientEventDocument;
import com.phorest.client.infrastructure.repository.document.ClientPayloadDocument;
import com.phorest.client.infrastructure.repository.spring.MongoClientEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoClientEventRepository eventRepository;

    @BeforeEach
    void clean() {
        eventRepository.deleteAll();
    }

    @Test
    void getClientsSkipsBannedClients() throws Exception {
        eventRepository.save(event("evt-1", "client-1", "CLIENT_REGISTERED", false, 1));
        eventRepository.save(event("evt-2", "client-2", "CLIENT_REGISTERED", false, 1));
        eventRepository.save(event("evt-3", "client-2", "CLIENT_BANNED", true, 2));

        mockMvc.perform(get("/api/v1/clients?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value("client-1"));
    }

    @Test
    void getClientReturns404ForBannedClient() throws Exception {
        eventRepository.save(event("evt-1", "client-1", "CLIENT_REGISTERED", false, 1));
        eventRepository.save(event("evt-2", "client-1", "CLIENT_BANNED", true, 2));

        mockMvc.perform(get("/api/v1/clients/client-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClientAppendsEvent() throws Exception {
        eventRepository.save(event("evt-1", "client-1", "CLIENT_REGISTERED", false, 1));

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

        assertThat(eventRepository.findByClientIdOrderByVersionAsc("client-1"))
                .extracting(ClientEventDocument::getType)
                .containsExactly("CLIENT_REGISTERED", "CLIENT_UPDATED");
    }

    @Test
    void banClientCreatesBanEventAndExcludesFromList() throws Exception {
        eventRepository.save(event("evt-1", "client-1", "CLIENT_REGISTERED", false, 1));

        mockMvc.perform(delete("/api/v1/clients/client-1"))
                .andExpect(status().isNoContent());

        assertThat(eventRepository.findByClientIdOrderByVersionAsc("client-1"))
                .extracting(ClientEventDocument::getType)
                .containsExactly("CLIENT_REGISTERED", "CLIENT_BANNED");

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    private ClientEventDocument event(String eventId, String clientId, String type, boolean banned, int version) {
        ClientEventDocument document = new ClientEventDocument();
        document.setEventId(eventId);
        document.setClientId(clientId);
        document.setType(type);
        document.setOccurredAt(Instant.now());
        document.setVersion(version);
        document.setPayload(new ClientPayloadDocument(
                clientId,
                "Jane",
                "Doe",
                clientId + "@example.com",
                "1234567890",
                "Female",
                banned
        ));
        return document;
    }
}
