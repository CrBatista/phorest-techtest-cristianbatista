package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.PurchaseResponse;
import com.phorest.booking.domain.purchase.PurchaseAggregate;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.domain.purchase.PurchaseEventStore;
import com.phorest.booking.domain.purchase.PurchaseSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PurchaseReadService {

    private final PurchaseEventStore eventStore;

    public PurchaseReadService(PurchaseEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Page<PurchaseResponse> getPurchases(Pageable pageable, String clientId) {
        List<PurchaseEvent> events = eventStore.loadAll();
        Map<String, List<PurchaseEvent>> byId = new LinkedHashMap<>();
        for (PurchaseEvent event : events) {
            byId.computeIfAbsent(event.purchaseId(), k -> new java.util.ArrayList<>()).add(event);
        }
        List<PurchaseResponse> responses = byId.values().stream()
                .map(PurchaseAggregate::fromEvents)
                .map(PurchaseAggregate::snapshot)
                .flatMap(Optional::stream)
                .filter(snapshot -> !snapshot.deleted())
                .filter(snapshot -> clientId == null || clientId.isBlank() || snapshot.clientId().equals(clientId))
                .map(this::toResponse)
                .toList();
        int total = responses.size();
        int from = Math.min((int) pageable.getOffset(), total);
        int to = Math.min(from + pageable.getPageSize(), total);
        List<PurchaseResponse> paged = responses.subList(from, to);
        return new PageImpl<>(paged, pageable, total);
    }

    public Optional<PurchaseResponse> getPurchase(String purchaseId) {
        return PurchaseAggregate.fromEvents(eventStore.loadByPurchaseId(purchaseId))
                .snapshot()
                .filter(snapshot -> !snapshot.deleted())
                .map(this::toResponse);
    }

    private PurchaseResponse toResponse(PurchaseSnapshot snapshot) {
        return new PurchaseResponse(
                snapshot.purchaseId(),
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.name(),
                snapshot.price(),
                snapshot.loyaltyPoints(),
                snapshot.performedAt()
        );
    }
}
