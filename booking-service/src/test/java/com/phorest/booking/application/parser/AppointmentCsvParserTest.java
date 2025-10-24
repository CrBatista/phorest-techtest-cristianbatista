package com.phorest.booking.application.parser;

import com.phorest.booking.application.config.BookingCsvProperties;
import com.phorest.booking.domain.appointment.AppointmentProfile;
import com.phorest.booking.domain.exception.BookingCsvParsingException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentCsvParserTest {

    private final AppointmentCsvParser parser = new AppointmentCsvParser(
            new BookingCsvProperties(
                    List.of("id", "client_id", "start_time", "end_time"),
                    List.of(),
                    List.of()
            ));

    @Test
    void parsesValidCsv() {
        String csv = """
                id,client_id,start_time,end_time
                app-1,client-1,2024-01-01 10:00:00 +0000,2024-01-01 11:00:00 +0000
                """;
        List<AppointmentProfile> profiles = parser.parse(stream(csv));
        assertThat(profiles).hasSize(1);
        assertThat(profiles.getFirst().clientId()).isEqualTo("client-1");
    }

    @Test
    void throwsWhenHeadersUnexpected() {
        String csv = """
                wrong,client_id,start_time,end_time
                app-1,client-1,2024-01-01 10:00:00 +0000,2024-01-01 11:00:00 +0000
                """;
        assertThatThrownBy(() -> parser.parse(stream(csv)))
                .isInstanceOf(BookingCsvParsingException.class);
    }

    private ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
