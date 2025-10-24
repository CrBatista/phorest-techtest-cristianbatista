package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.events.ServiceEventPublisher;
import com.phorest.booking.application.parser.ServiceCsvParser;
import com.phorest.booking.application.parser.ServiceCsvParser.ServiceCsvRecord;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.serviceitem.ServiceAggregate;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.domain.serviceitem.ServiceEventStore;
import com.phorest.booking.domain.serviceitem.ServiceEventType;
import com.phorest.booking.domain.serviceitem.ServiceProfile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceImportService {

    private final ServiceCsvParser parser;
    private final ServiceEventStore eventStore;
    private final AppointmentQueryService appointmentQueryService;
    private final ServiceEventPublisher eventPublisher;
    private final Clock clock;

    public ServiceImportService(ServiceCsvParser parser,
                                ServiceEventStore eventStore,
                                AppointmentQueryService appointmentQueryService,
                                ServiceEventPublisher eventPublisher,
                                Clock clock) {
        this.parser = parser;
        this.eventStore = eventStore;
        this.appointmentQueryService = appointmentQueryService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public ImportSummary importServices(MultipartFile file) {
        List<ServiceCsvRecord> records = parse(file);
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int deleted = 0;

        for (ServiceCsvRecord record : records) {
            AppointmentSnapshot appointment = appointmentQueryService.getSnapshot(record.appointmentId()).orElse(null);
            if (appointment == null || appointment.deleted()) {
                skipped++;
                continue;
            }

            ServiceProfile profile = new ServiceProfile(
                    record.serviceId(),
                    record.appointmentId(),
                    appointment.clientId(),
                    record.name(),
                    record.price(),
                    record.loyaltyPoints(),
                    appointment.startTime(),
                    false
            );

            ServiceAggregate aggregate = ServiceAggregate.fromEvents(eventStore.loadByServiceId(record.serviceId()));
            var nextType = aggregate.nextEventType(profile);
            if (nextType.isEmpty()) {
                skipped++;
                continue;
            }

            ServiceEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
            eventStore.append(event);
            eventPublisher.publish(event);

            if (nextType.get() == ServiceEventType.SERVICE_CREATED) {
                created++;
            } else if (nextType.get() == ServiceEventType.SERVICE_UPDATED) {
                updated++;
            } else {
                deleted++;
            }
        }

        return new ImportSummary(records.size(), created, updated, skipped, deleted);
    }

    private List<ServiceCsvRecord> parse(MultipartFile file) {
        try {
            return parser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read provided CSV file", e);
        }
    }

    private ServiceEvent buildEvent(ServiceProfile profile, ServiceEventType type, int version) {
        Instant occurredAt = Instant.now(clock);
        return new ServiceEvent(
                UUID.randomUUID().toString(),
                profile.serviceId(),
                type,
                profile,
                occurredAt,
                version
        );
    }
}
