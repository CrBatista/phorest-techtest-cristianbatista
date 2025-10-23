package com.phorest.client.application.parser;

import com.phorest.client.application.config.ClientCsvProperties;
import com.phorest.client.domain.exception.ClientCsvParsingException;
import com.phorest.client.domain.model.ClientProfile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientCsvParserTest {

    private final ClientCsvParser parser = new ClientCsvParser(
            new ClientCsvProperties(List.of("id", "first_name", "last_name", "email", "phone", "gender", "banned"))
    );

    @Test
    void parsesValidCsvIntoProfiles() {
        ByteArrayInputStream csv = csv("""
                id,first_name,last_name,email,phone,gender,banned
                client-1,Jane,Doe,jane.doe@example.com,1234567890,Female,false
                client-2,John,Smith,john.smith@example.com,9876543210,Male,true
                """);

        List<ClientProfile> profiles = parser.parse(csv);

        assertThat(profiles)
                .hasSize(2)
                .extracting(ClientProfile::clientId)
                .containsExactly("client-1", "client-2");
        assertThat(profiles.get(1).banned()).isTrue();
    }

    @Test
    void rejectsCsvWithUnexpectedHeaderOrder() {
        ByteArrayInputStream csv = csv("""
                id,last_name,first_name,email,phone,gender,banned
                client-1,Doe,Jane,jane@example.com,123,Female,false
                """);

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(ClientCsvParsingException.class)
                .hasMessageContaining("expected order");
    }

    @Test
    void rejectsCsvWhenClientIdEmpty() {
        ByteArrayInputStream csv = csv("""
                id,first_name,last_name,email,phone,gender,banned
                ,Jane,Doe,jane@example.com,123,Female,false
                """);

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(ClientCsvParsingException.class)
                .hasMessageContaining("Client id");
    }

    private ByteArrayInputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
