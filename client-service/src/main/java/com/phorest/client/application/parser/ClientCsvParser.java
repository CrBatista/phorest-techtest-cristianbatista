package com.phorest.client.application.parser;

import com.phorest.client.application.config.ClientCsvProperties;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.exception.ClientCsvParsingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientCsvParser {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "id", "first_name", "last_name", "email", "phone", "gender", "banned"
    );

    private final ClientCsvProperties properties;

    public ClientCsvParser(ClientCsvProperties properties) {
        this.properties = properties;
    }

    public List<ClientProfile> parse(InputStream inputStream) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, format)) {

            validateHeaders(csvParser);
            List<ClientProfile> profiles = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                profiles.add(toProfile(record));
            }

            return profiles;
        } catch (IOException ex) {
            throw new ClientCsvParsingException("Failed to read CSV content", ex);
        }
    }

    private void validateHeaders(CSVParser parser) {
        List<String> headers = parser.getHeaderNames();
        if (headers == null || headers.isEmpty()) {
            throw new ClientCsvParsingException("CSV headers are missing");
        }

        Set<String> lowerCaseHeaders = headers.stream()
                .map(header -> header.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (!lowerCaseHeaders.containsAll(REQUIRED_HEADERS)) {
            throw new ClientCsvParsingException("CSV headers do not match expected schema");
        }

        if (properties.expectedHeaders() != null && !properties.expectedHeaders().isEmpty()) {
            List<String> normalized = properties.expectedHeaders().stream()
                    .map(header -> header.toLowerCase(Locale.ROOT))
                    .toList();
            if (!normalized.equals(headers.stream()
                    .map(header -> header.toLowerCase(Locale.ROOT))
                    .toList())) {
                throw new ClientCsvParsingException("CSV headers are not in the expected order");
            }
        }
    }

    private ClientProfile toProfile(CSVRecord record) {
        String clientId = record.get("id");
        if (!StringUtils.hasText(clientId)) {
            throw new ClientCsvParsingException("Client id cannot be empty");
        }

        return new ClientProfile(
                clientId,
                record.get("first_name"),
                record.get("last_name"),
                record.get("email"),
                record.get("phone"),
                record.get("gender"),
                parseBoolean(record.get("banned"))
        );
    }

    private boolean parseBoolean(String value) {
        return Boolean.parseBoolean(value.trim());
    }
}
