package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.PurchaseUpdateRequest;
import com.phorest.booking.application.events.PurchaseEventPublisher;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.domain.purchase.PurchaseEventStore;
import com.phorest.booking.domain.purchase.PurchaseEventType;
import com.phorest.booking.domain.purchase.PurchaseProfile;
import com.phorest.booking.domain.purchase.PurchaseSnapshot;
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
class PurchaseCommandServiceTest {

    @Mock
    private PurchaseEventStore eventStore;

    @Mock
    private AppointmentQueryService appointmentQueryService;

    @Mock
    private PurchaseEventPublisher eventPublisher;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private PurchaseCommandService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseCommandService(eventStore, appointmentQueryService, eventPublisher, clock);
    }

    @Test
    void updatePurchasePublishesEvent() {
        PurchaseProfile existingProfile = new PurchaseProfile("purchase-1", "appointment-1", "client-1", "Shampoo", BigDecimal.ONE, 10, Instant.parse("2024-01-01T10:00:00Z"), false);
        PurchaseEvent existingEvent = new PurchaseEvent(UUID.randomUUID().toString(), "purchase-1", PurchaseEventType.PURCHASE_CREATED, existingProfile, Instant.now(clock), 1);
        when(eventStore.loadByPurchaseId("purchase-1")).thenReturn(List.of(existingEvent));
        when(appointmentQueryService.getSnapshot("appointment-1")).thenReturn(Optional.of(new AppointmentSnapshot("appointment-1", "client-2", Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T11:00:00Z"), false, 1)));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseUpdateRequest request = new PurchaseUpdateRequest("appointment-1", "Conditioner", BigDecimal.valueOf(25), 30, Instant.parse("2024-01-01T10:00:00Z"));
        var response = service.updatePurchase("purchase-1", request);

        assertThat(response).isPresent();
        ArgumentCaptor<PurchaseEvent> captor = ArgumentCaptor.forClass(PurchaseEvent.class);
        verify(eventStore).append(captor.capture());
        verify(eventPublisher).publish(captor.getValue());
        assertThat(captor.getValue().type()).isEqualTo(PurchaseEventType.PURCHASE_UPDATED);
    }

    @Test
    void deletePurchaseAppendsDeleteEvent() {
        PurchaseProfile existingProfile = new PurchaseProfile("purchase-1", "appointment-1", "client-1", "Shampoo", BigDecimal.ONE, 10, Instant.parse("2024-01-01T10:00:00Z"), false);
        PurchaseEvent existingEvent = new PurchaseEvent(UUID.randomUUID().toString(), "purchase-1", PurchaseEventType.PURCHASE_CREATED, existingProfile, Instant.now(clock), 1);
        when(eventStore.loadByPurchaseId("purchase-1")).thenReturn(List.of(existingEvent));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean deleted = service.deletePurchase("purchase-1");

        assertThat(deleted).isTrue();
        verify(eventPublisher).publish(any());
    }
}
