package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.events.PurchaseEventPublisher;
import com.phorest.booking.application.parser.PurchaseCsvParser;
import com.phorest.booking.application.parser.PurchaseCsvParser.PurchaseCsvRecord;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.domain.purchase.PurchaseEventStore;
import com.phorest.booking.domain.purchase.PurchaseEventType;
import com.phorest.booking.domain.purchase.PurchaseProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseImportServiceTest {

    @Mock
    private PurchaseCsvParser parser;

    @Mock
    private PurchaseEventStore eventStore;

    @Mock
    private AppointmentQueryService appointmentQueryService;

    @Mock
    private PurchaseEventPublisher eventPublisher;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private PurchaseImportService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseImportService(parser, eventStore, appointmentQueryService, eventPublisher, clock);
    }

    @Test
    void importsNewPurchase() {
        PurchaseCsvRecord record = new PurchaseCsvRecord("purchase-1", "appointment-1", "Shampoo", BigDecimal.valueOf(19.5), 20);
        when(parser.parse(any())).thenReturn(List.of(record));
        when(eventStore.loadByPurchaseId("purchase-1")).thenReturn(List.of());
        when(appointmentQueryService.getSnapshot("appointment-1")).thenReturn(Optional.of(new AppointmentSnapshot("appointment-1", "client-1", Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T11:00:00Z"), false, 1)));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ImportSummary summary = service.importPurchases(new MockMultipartFile("file", new byte[0]));

        assertThat(summary).isEqualTo(new ImportSummary(1, 1, 0, 0, 0));
        ArgumentCaptor<PurchaseEvent> captor = ArgumentCaptor.forClass(PurchaseEvent.class);
        verify(eventStore).append(captor.capture());
        verify(eventPublisher).publish(captor.getValue());
        assertThat(captor.getValue().type()).isEqualTo(PurchaseEventType.PURCHASE_CREATED);
    }

    @Test
    void skipsWhenAppointmentMissing() {
        PurchaseCsvRecord record = new PurchaseCsvRecord("purchase-1", "appointment-1", "Shampoo", BigDecimal.valueOf(19.5), 20);
        when(parser.parse(any())).thenReturn(List.of(record));
        when(appointmentQueryService.getSnapshot("appointment-1")).thenReturn(Optional.empty());

        ImportSummary summary = service.importPurchases(new MockMultipartFile("file", new byte[0]));

        assertThat(summary).isEqualTo(new ImportSummary(1, 0, 0, 1, 0));
        verify(eventStore, never()).append(any());
        verify(eventPublisher, never()).publish(any());
    }
}
