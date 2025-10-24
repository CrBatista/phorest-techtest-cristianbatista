package com.phorest.booking.application.parser;

import com.phorest.booking.application.config.BookingCsvProperties;
import com.phorest.booking.domain.exception.BookingCsvParsingException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceCsvParserTest {

    private final ServiceCsvParser parser = new ServiceCsvParser(
            new BookingCsvProperties(
                    List.of("id", "client_id", "start_time", "end_time"),
                    List.of("id", "appointment_id", "name", "price", "loyalty_points"),
                    List.of()
            ));

    @Test
    void parsesValidCsvRecord() {
        String csv = """
                id,appointment_id,name,price,loyalty_points
                service-1,app-1,Full Head Colour,85.0,80
                """;
        var records = parser.parse(stream(csv));
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().price()).isEqualByComparingTo("85.0");
    }

    @Test
    void throwsOnInvalidHeaders() {
        String csv = """
                wrong,appointment_id,name,price,loyalty_points
                service-1,app-1,Full Head Colour,85.0,80
                """;
        assertThatThrownBy(() -> parser.parse(stream(csv)))
                .isInstanceOf(BookingCsvParsingException.class);
    }

    private ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
