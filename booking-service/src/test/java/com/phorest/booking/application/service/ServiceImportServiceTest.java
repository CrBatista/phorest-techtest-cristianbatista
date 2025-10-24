package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.events.ServiceEventPublisher;
import com.phorest.booking.application.parser.ServiceCsvParser;
import com.phorest.booking.application.parser.ServiceCsvParser.ServiceCsvRecord;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.domain.serviceitem.ServiceEventStore;
import com.phorest.booking.domain.serviceitem.ServiceEventType;
import com.phorest.booking.domain.serviceitem.ServiceProfile;
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
class ServiceImportServiceTest {

    @Mock
    private ServiceCsvParser parser;

    @Mock
    private ServiceEventStore eventStore;

    @Mock
    private AppointmentQueryService appointmentQueryService;

    @Mock
    private ServiceEventPublisher eventPublisher;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private ServiceImportService service;

    @BeforeEach
    void setUp() {
        service = new ServiceImportService(parser, eventStore, appointmentQueryService, eventPublisher, clock);
    }

    @Test
    void importsNewService() {
        ServiceCsvRecord record = new ServiceCsvRecord("service-1", "appointment-1", "Full Head Colour", BigDecimal.valueOf(85), 80);
        when(parser.parse(any())).thenReturn(List.of(record));
        when(eventStore.loadByServiceId("service-1")).thenReturn(List.of());
        when(appointmentQueryService.getSnapshot("appointment-1")).thenReturn(Optional.of(new AppointmentSnapshot("appointment-1", "client-1", Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T11:00:00Z"), false, 1)));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ImportSummary summary = service.importServices(new MockMultipartFile("file", new byte[0]));

        assertThat(summary).isEqualTo(new ImportSummary(1, 1, 0, 0, 0));
        ArgumentCaptor<ServiceEvent> captor = ArgumentCaptor.forClass(ServiceEvent.class);
        verify(eventStore).append(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(ServiceEventType.SERVICE_CREATED);
        verify(eventPublisher).publish(captor.getValue());
    }

    @Test
    void skipsWhenAppointmentMissing() {
        ServiceCsvRecord record = new ServiceCsvRecord("service-1", "appointment-1", "Full Head Colour", BigDecimal.valueOf(85), 80);
        when(parser.parse(any())).thenReturn(List.of(record));
        when(appointmentQueryService.getSnapshot("appointment-1")).thenReturn(Optional.empty());

        ImportSummary summary = service.importServices(new MockMultipartFile("file", new byte[0]));

        assertThat(summary).isEqualTo(new ImportSummary(1, 0, 0, 1, 0));
        verify(eventStore, never()).append(any());
        verify(eventPublisher, never()).publish(any());
    }
}
