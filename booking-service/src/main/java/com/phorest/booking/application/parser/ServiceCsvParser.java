package com.phorest.booking.application.parser;

import com.phorest.booking.application.config.BookingCsvProperties;
import com.phorest.booking.domain.exception.BookingCsvParsingException;
import com.phorest.booking.domain.serviceitem.ServiceProfile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ServiceCsvParser {

    private static final Set<String> REQUIRED_HEADERS = Set.of("id", "appointment_id", "name", "price", "loyalty_points");

    private final BookingCsvProperties properties;

    public ServiceCsvParser(BookingCsvProperties properties) {
        this.properties = properties;
    }

    public List<ServiceCsvRecord> parse(InputStream inputStream) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, format)) {

            validateHeaders(csvParser);
            List<ServiceCsvRecord> records = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                records.add(toRecord(record));
            }
            return records;
        } catch (IOException ex) {
            throw new BookingCsvParsingException("Failed to read services CSV", ex);
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
        if (properties.servicesHeaders() != null && !properties.servicesHeaders().isEmpty()) {
            List<String> expected = properties.servicesHeaders().stream()
                    .map(h -> h.toLowerCase(Locale.ROOT))
                    .toList();
            List<String> actual = headers.stream().map(h -> h.toLowerCase(Locale.ROOT)).toList();
            if (!expected.equals(actual)) {
                throw new BookingCsvParsingException("CSV headers are not in the expected order");
            }
        }
    }

    private ServiceCsvRecord toRecord(CSVRecord record) {
        String id = record.get("id");
        if (!StringUtils.hasText(id)) {
            throw new BookingCsvParsingException("Service id cannot be empty");
        }
        String appointmentId = record.get("appointment_id");
        String name = record.get("name");
        BigDecimal price = parsePrice(record.get("price"));
        int loyaltyPoints = parsePoints(record.get("loyalty_points"));
        return new ServiceCsvRecord(id, appointmentId, name, price, loyaltyPoints);
    }

    private BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new BookingCsvParsingException("Invalid price value: " + value, ex);
        }
    }

    private int parsePoints(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new BookingCsvParsingException("Invalid loyalty_points value: " + value, ex);
        }
    }

    public record ServiceCsvRecord(String serviceId,
                                   String appointmentId,
                                   String name,
                                   BigDecimal price,
                                   int loyaltyPoints) {
    }
}
