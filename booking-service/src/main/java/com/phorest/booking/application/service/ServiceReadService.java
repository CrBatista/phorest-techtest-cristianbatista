package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ServiceResponse;
import com.phorest.booking.domain.serviceitem.ServiceAggregate;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.domain.serviceitem.ServiceEventStore;
import com.phorest.booking.domain.serviceitem.ServiceSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ServiceReadService {

    private final ServiceEventStore eventStore;

    public ServiceReadService(ServiceEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Page<ServiceResponse> getServices(Pageable pageable, String clientId) {
        List<ServiceEvent> events = eventStore.loadAll();
        Map<String, List<ServiceEvent>> byId = new LinkedHashMap<>();
        for (ServiceEvent event : events) {
            byId.computeIfAbsent(event.serviceId(), k -> new java.util.ArrayList<>()).add(event);
        }
        List<ServiceResponse> responses = byId.values().stream()
                .map(ServiceAggregate::fromEvents)
                .map(ServiceAggregate::snapshot)
                .flatMap(Optional::stream)
                .filter(snapshot -> !snapshot.deleted())
                .filter(snapshot -> clientId == null || clientId.isBlank() || snapshot.clientId().equals(clientId))
                .map(this::toResponse)
                .toList();
        int total = responses.size();
        int from = Math.min((int) pageable.getOffset(), total);
        int to = Math.min(from + pageable.getPageSize(), total);
        List<ServiceResponse> paged = responses.subList(from, to);
        return new PageImpl<>(paged, pageable, total);
    }

    public Optional<ServiceResponse> getService(String serviceId) {
        return ServiceAggregate.fromEvents(eventStore.loadByServiceId(serviceId))
                .snapshot()
                .filter(snapshot -> !snapshot.deleted())
                .map(this::toResponse);
    }

    private ServiceResponse toResponse(ServiceSnapshot snapshot) {
        return new ServiceResponse(
                snapshot.serviceId(),
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.name(),
                snapshot.price(),
                snapshot.loyaltyPoints(),
                snapshot.performedAt()
        );
    }
}
