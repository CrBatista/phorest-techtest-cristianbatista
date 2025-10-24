package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.AppointmentResponse;
import com.phorest.booking.domain.appointment.AppointmentAggregate;
import com.phorest.booking.domain.appointment.AppointmentEvent;
import com.phorest.booking.domain.appointment.AppointmentEventStore;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentReadService {

    private final AppointmentEventStore eventStore;

    public AppointmentReadService(AppointmentEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Page<AppointmentResponse> getAppointments(Pageable pageable, String clientId) {
        List<AppointmentEvent> events = eventStore.loadAll();
        Map<String, List<AppointmentEvent>> byId = new LinkedHashMap<>();
        for (AppointmentEvent event : events) {
            byId.computeIfAbsent(event.appointmentId(), k -> new java.util.ArrayList<>()).add(event);
        }
        List<AppointmentResponse> responses = byId.values().stream()
                .map(AppointmentAggregate::fromEvents)
                .map(AppointmentAggregate::snapshot)
                .flatMap(Optional::stream)
                .filter(snapshot -> !snapshot.deleted())
                .filter(snapshot -> clientId == null || clientId.isBlank() || snapshot.clientId().equals(clientId))
                .map(this::toResponse)
                .toList();

        int total = responses.size();
        int from = Math.min((int) pageable.getOffset(), total);
        int to = Math.min(from + pageable.getPageSize(), total);
        List<AppointmentResponse> paged = responses.subList(from, to);
        return new PageImpl<>(paged, pageable, total);
    }

    public Optional<AppointmentResponse> getAppointment(String appointmentId) {
        return AppointmentAggregate.fromEvents(eventStore.loadByAppointmentId(appointmentId))
                .snapshot()
                .filter(snapshot -> !snapshot.deleted())
                .map(this::toResponse);
    }

    private AppointmentResponse toResponse(AppointmentSnapshot snapshot) {
        return new AppointmentResponse(
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.startTime(),
                snapshot.endTime()
        );
    }
}
