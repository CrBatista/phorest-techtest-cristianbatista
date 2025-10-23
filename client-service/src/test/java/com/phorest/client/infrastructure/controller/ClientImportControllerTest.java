package com.phorest.client.infrastructure.controller;

import com.phorest.client.infrastructure.repository.document.ClientEventDocument;
import com.phorest.client.infrastructure.repository.spring.MongoClientEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClientImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoClientEventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldImportClientsFromCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "clients.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csv("""
                        id,first_name,last_name,email,phone,gender,banned
                        client-1,Jane,Doe,jane.doe@example.com,1234567890,Female,false
                        client-2,John,Smith,john.smith@example.com,9876543210,Male,true
                        """).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/import/clients").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(2))
                .andExpect(jsonPath("$.created").value(2))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0));

        List<ClientEventDocument> client1Events = eventRepository.findByClientIdOrderByVersionAsc("client-1");
        assertThat(client1Events)
                .hasSize(1)
                .first()
                .extracting(ClientEventDocument::getType)
                .isEqualTo("CLIENT_REGISTERED");
    }

    @Test
    void shouldAppendUpdateEventsWhenClientDataChanges() throws Exception {
        MockMultipartFile initialFile = new MockMultipartFile(
                "file",
                "clients.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csv("""
                        id,first_name,last_name,email,phone,gender,banned
                        client-1,Jane,Doe,jane.doe@example.com,1234567890,Female,false
                        """).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/import/clients").file(initialFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1));

        MockMultipartFile updatedFile = new MockMultipartFile(
                "file",
                "clients.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csv("""
                        id,first_name,last_name,email,phone,gender,banned
                        client-1,Jane,Doe,jane.new@example.com,1234567890,Female,false
                        """).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/import/clients").file(updatedFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.updated").value(1))
                .andExpect(jsonPath("$.skipped").value(0));

        List<ClientEventDocument> events = eventRepository.findByClientIdOrderByVersionAsc("client-1");
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getType()).isEqualTo("CLIENT_REGISTERED");
        assertThat(events.get(1).getType()).isEqualTo("CLIENT_UPDATED");
        assertThat(events.get(1).getVersion()).isEqualTo(2);
    }

    private String csv(String content) {
        return content.stripIndent();
    }
}
