package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ServiceUpdateRequest;
import com.phorest.booking.application.events.ServiceEventPublisher;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.domain.serviceitem.ServiceEventStore;
import com.phorest.booking.domain.serviceitem.ServiceEventType;
import com.phorest.booking.domain.serviceitem.ServiceProfile;
import com.phorest.booking.domain.serviceitem.ServiceSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCommandServiceTest {

    @Mock
    private ServiceEventStore eventStore;

    @Mock
    private AppointmentQueryService appointmentQueryService;

    @Mock
    private ServiceEventPublisher eventPublisher;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private ServiceCommandService service;

    @BeforeEach
    void setUp() {
        service = new ServiceCommandService(eventStore, appointmentQueryService, eventPublisher, clock);
    }

    @Test
    void updateServicePublishesEvent() {
        ServiceProfile existingProfile = new ServiceProfile("service-1", "appointment-1", "client-1", "Old", BigDecimal.TEN, 10, Instant.parse("2024-01-01T10:00:00Z"), false);
        ServiceEvent existingEvent = new ServiceEvent(UUID.randomUUID().toString(), "service-1", ServiceEventType.SERVICE_CREATED, existingProfile, Instant.now(clock), 1);
        when(eventStore.loadByServiceId("service-1")).thenReturn(List.of(existingEvent));
        when(appointmentQueryService.getSnapshot("appointment-1")).thenReturn(Optional.of(new AppointmentSnapshot("appointment-1", "client-2", Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T11:00:00Z"), false, 1)));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceUpdateRequest request = new ServiceUpdateRequest("appointment-1", "New", BigDecimal.valueOf(20), 30, Instant.parse("2024-01-01T10:00:00Z"));
        var response = service.updateService("service-1", request);

        assertThat(response).isPresent();
        assertThat(response.get().clientId()).isEqualTo("client-2");
        ArgumentCaptor<ServiceEvent> captor = ArgumentCaptor.forClass(ServiceEvent.class);
        verify(eventStore).append(captor.capture());
        verify(eventPublisher).publish(captor.getValue());
        assertThat(captor.getValue().type()).isEqualTo(ServiceEventType.SERVICE_UPDATED);
    }

    @Test
    void deleteServiceAppendsDeleteEvent() {
        ServiceProfile existingProfile = new ServiceProfile("service-1", "appointment-1", "client-1", "Old", BigDecimal.TEN, 10, Instant.parse("2024-01-01T10:00:00Z"), false);
        ServiceEvent existingEvent = new ServiceEvent(UUID.randomUUID().toString(), "service-1", ServiceEventType.SERVICE_CREATED, existingProfile, Instant.now(clock), 1);
        when(eventStore.loadByServiceId("service-1")).thenReturn(List.of(existingEvent));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean deleted = service.deleteService("service-1");

        assertThat(deleted).isTrue();
        verify(eventPublisher).publish(any());
    }
}
