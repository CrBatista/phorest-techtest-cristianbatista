package com.phorest.booking.application.parser;

import com.phorest.booking.application.config.BookingCsvProperties;
import com.phorest.booking.domain.exception.BookingCsvParsingException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseCsvParserTest {

    private final PurchaseCsvParser parser = new PurchaseCsvParser(
            new BookingCsvProperties(
                    List.of("id", "client_id", "start_time", "end_time"),
                    List.of(),
                    List.of("id", "appointment_id", "name", "price", "loyalty_points")
            ));

    @Test
    void parsesValidRecords() {
        String csv = """
                id,appointment_id,name,price,loyalty_points
                purchase-1,app-1,Shampoo,19.5,20
                """;
        var records = parser.parse(stream(csv));
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().loyaltyPoints()).isEqualTo(20);
    }

    @Test
    void throwsOnBadHeaders() {
        String csv = """
                wrong,appointment_id,name,price,loyalty_points
                purchase-1,app-1,Shampoo,19.5,20
                """;
        assertThatThrownBy(() -> parser.parse(stream(csv)))
                .isInstanceOf(BookingCsvParsingException.class);
    }

    private ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
