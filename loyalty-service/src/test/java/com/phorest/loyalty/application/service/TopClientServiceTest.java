package com.phorest.loyalty.application.service;

import com.phorest.loyalty.application.dto.TopClientResponse;
import com.phorest.loyalty.domain.purchase.PurchaseEvent;
import com.phorest.loyalty.domain.purchase.PurchaseEventStore;
import com.phorest.loyalty.domain.purchase.PurchaseEventType;
import com.phorest.loyalty.domain.purchase.PurchaseProfile;
import com.phorest.loyalty.domain.serviceitem.ServiceEvent;
import com.phorest.loyalty.domain.serviceitem.ServiceEventStore;
import com.phorest.loyalty.domain.serviceitem.ServiceEventType;
import com.phorest.loyalty.domain.serviceitem.ServiceProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopClientServiceTest {

    @Mock
    private ServiceEventStore serviceEventStore;

    @Mock
    private PurchaseEventStore purchaseEventStore;

    @Test
    void aggregatesLoyaltyPointsAcrossServicesAndPurchases() {
        ServiceEvent serviceEvent = new ServiceEvent(
                "evt-1",
                "service-1",
                ServiceEventType.SERVICE_CREATED,
                new ServiceProfile("service-1", "appointment-1", "client-1", "Colour", BigDecimal.valueOf(50), 40, Instant.parse("2024-01-05T10:00:00Z"), false),
                Instant.parse("2024-01-01T00:00:00Z"),
                1
        );
        PurchaseEvent purchaseEvent = new PurchaseEvent(
                "evt-2",
                "purchase-1",
                PurchaseEventType.PURCHASE_CREATED,
                new PurchaseProfile("purchase-1", "appointment-2", "client-2", "Shampoo", BigDecimal.valueOf(20), 20, Instant.parse("2024-01-10T10:00:00Z"), false),
                Instant.parse("2024-01-02T00:00:00Z"),
                1
        );
        when(serviceEventStore.loadAll()).thenReturn(List.of(serviceEvent));
        when(purchaseEventStore.loadAll()).thenReturn(List.of(purchaseEvent));

        TopClientService service = new TopClientService(serviceEventStore, purchaseEventStore);
        List<TopClientResponse> topClients = service.topClients(10, Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-02-01T00:00:00Z"));

        assertThat(topClients).extracting(TopClientResponse::clientId).containsExactly("client-1", "client-2");
        assertThat(topClients.getFirst().loyaltyPoints()).isEqualTo(40);
    }
}
