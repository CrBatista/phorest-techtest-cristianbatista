package com.phorest.loyalty.application.service;

import com.phorest.loyalty.application.dto.TopClientResponse;
import com.phorest.loyalty.domain.purchase.PurchaseAggregate;
import com.phorest.loyalty.domain.purchase.PurchaseEvent;
import com.phorest.loyalty.domain.purchase.PurchaseEventStore;
import com.phorest.loyalty.domain.purchase.PurchaseSnapshot;
import com.phorest.loyalty.domain.serviceitem.ServiceAggregate;
import com.phorest.loyalty.domain.serviceitem.ServiceEvent;
import com.phorest.loyalty.domain.serviceitem.ServiceEventStore;
import com.phorest.loyalty.domain.serviceitem.ServiceSnapshot;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TopClientService {

    private final ServiceEventStore serviceEventStore;
    private final PurchaseEventStore purchaseEventStore;

    public TopClientService(ServiceEventStore serviceEventStore,
                            PurchaseEventStore purchaseEventStore) {
        this.serviceEventStore = serviceEventStore;
        this.purchaseEventStore = purchaseEventStore;
    }

    public List<TopClientResponse> topClients(int limit, Instant fromInclusive, Instant toExclusive) {
        Map<String, Integer> loyaltyByClient = new LinkedHashMap<>();

        loadServiceSnapshots().stream()
                .filter(snapshot -> !snapshot.deleted())
                .filter(snapshot -> withinRange(snapshot.performedAt(), fromInclusive, toExclusive))
                .forEach(snapshot -> loyaltyByClient.merge(snapshot.clientId(), snapshot.loyaltyPoints(), Integer::sum));

        loadPurchaseSnapshots().stream()
                .filter(snapshot -> !snapshot.deleted())
                .filter(snapshot -> withinRange(snapshot.performedAt(), fromInclusive, toExclusive))
                .forEach(snapshot -> loyaltyByClient.merge(snapshot.clientId(), snapshot.loyaltyPoints(), Integer::sum));

        return loyaltyByClient.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> new TopClientResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private boolean withinRange(Instant performedAt, Instant fromInclusive, Instant toExclusive) {
        boolean afterFrom = fromInclusive == null || !performedAt.isBefore(fromInclusive);
        boolean beforeTo = toExclusive == null || performedAt.isBefore(toExclusive);
        return afterFrom && beforeTo;
    }

    private List<ServiceSnapshot> loadServiceSnapshots() {
        Map<String, List<ServiceEvent>> byId = new LinkedHashMap<>();
        for (ServiceEvent event : serviceEventStore.loadAll()) {
            byId.computeIfAbsent(event.serviceId(), key -> new java.util.ArrayList<>()).add(event);
        }
        return byId.values().stream()
                .map(ServiceAggregate::fromEvents)
                .map(ServiceAggregate::snapshot)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<PurchaseSnapshot> loadPurchaseSnapshots() {
        Map<String, List<PurchaseEvent>> byId = new LinkedHashMap<>();
        for (PurchaseEvent event : purchaseEventStore.loadAll()) {
            byId.computeIfAbsent(event.purchaseId(), key -> new java.util.ArrayList<>()).add(event);
        }
        return byId.values().stream()
                .map(PurchaseAggregate::fromEvents)
                .map(PurchaseAggregate::snapshot)
                .flatMap(Optional::stream)
                .toList();
    }
}
