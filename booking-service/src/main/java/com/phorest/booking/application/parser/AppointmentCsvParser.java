package com.phorest.booking.application.parser;

import com.phorest.booking.application.config.BookingCsvProperties;
import com.phorest.booking.domain.appointment.AppointmentProfile;
import com.phorest.booking.domain.exception.BookingCsvParsingException;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AppointmentCsvParser {

    private static final Set<String> REQUIRED_HEADERS = Set.of("id", "client_id", "start_time", "end_time");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

    private final BookingCsvProperties properties;

    public AppointmentCsvParser(BookingCsvProperties properties) {
        this.properties = properties;
    }

    public List<AppointmentProfile> parse(InputStream inputStream) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, format)) {

            validateHeaders(csvParser);
            List<AppointmentProfile> profiles = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                profiles.add(toProfile(record));
            }
            return profiles;
        } catch (IOException e) {
            throw new BookingCsvParsingException("Failed to read appointments CSV", e);
        }
    }

    private void validateHeaders(CSVParser parser) {
        List<String> headers = parser.getHeaderNames();
        if (headers == null || headers.isEmpty()) {
            throw new BookingCsvParsingException("CSV headers are missing");
        }
        Set<String> lower = headers.stream().map(h -> h.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        if (!lower.containsAll(REQUIRED_HEADERS)) {
            throw new BookingCsvParsingException("CSV headers do not match expected schema");
        }
        if (properties.appointmentsHeaders() != null && !properties.appointmentsHeaders().isEmpty()) {
            List<String> expected = properties.appointmentsHeaders().stream()
                    .map(h -> h.toLowerCase(Locale.ROOT))
                    .toList();
            List<String> actual = headers.stream().map(h -> h.toLowerCase(Locale.ROOT)).toList();
            if (!expected.equals(actual)) {
                throw new BookingCsvParsingException("CSV headers are not in the expected order");
            }
        }
    }

    private AppointmentProfile toProfile(CSVRecord record) {
        String id = record.get("id");
        if (!StringUtils.hasText(id)) {
            throw new BookingCsvParsingException("Appointment id cannot be empty");
        }
        String clientId = record.get("client_id");
        Instant start = parseDate(record.get("start_time"));
        Instant end = parseDate(record.get("end_time"));
        return new AppointmentProfile(id, clientId, start, end, false);
    }

    private Instant parseDate(String value) {
        try {
            return OffsetDateTime.parse(value, FORMATTER).toInstant();
        } catch (Exception ex) {
            throw new BookingCsvParsingException("Invalid date format: " + value, ex);
        }
    }
}
