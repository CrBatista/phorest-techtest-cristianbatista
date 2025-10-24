package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.parser.AppointmentCsvParser;
import com.phorest.booking.domain.appointment.AppointmentAggregate;
import com.phorest.booking.domain.appointment.AppointmentEvent;
import com.phorest.booking.domain.appointment.AppointmentEventStore;
import com.phorest.booking.domain.appointment.AppointmentEventType;
import com.phorest.booking.domain.appointment.AppointmentProfile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentImportService {

    private final AppointmentCsvParser parser;
    private final AppointmentEventStore eventStore;
    private final Clock clock;

    public AppointmentImportService(AppointmentCsvParser parser,
                                    AppointmentEventStore eventStore,
                                    Clock clock) {
        this.parser = parser;
        this.eventStore = eventStore;
        this.clock = clock;
    }

    public ImportSummary importAppointments(MultipartFile file) {
        List<AppointmentProfile> profiles = parse(file);
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int deleted = 0;

        for (AppointmentProfile profile : profiles) {
            AppointmentAggregate aggregate = AppointmentAggregate.fromEvents(eventStore.loadByAppointmentId(profile.appointmentId()));
            var nextType = aggregate.nextEventType(profile);
            if (nextType.isEmpty()) {
                skipped++;
                continue;
            }
            AppointmentEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
            eventStore.append(event);
            if (nextType.get() == AppointmentEventType.APPOINTMENT_REGISTERED) {
                created++;
            } else if (nextType.get() == AppointmentEventType.APPOINTMENT_UPDATED) {
                updated++;
            } else {
                deleted++;
            }
        }

        return new ImportSummary(profiles.size(), created, updated, skipped, deleted);
    }

    private List<AppointmentProfile> parse(MultipartFile file) {
        try {
            return parser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read provided CSV file", e);
        }
    }

    private AppointmentEvent buildEvent(AppointmentProfile profile, AppointmentEventType type, int version) {
        Instant occurredAt = Instant.now(clock);
        return new AppointmentEvent(
                UUID.randomUUID().toString(),
                profile.appointmentId(),
                type,
                profile,
                occurredAt,
                version
        );
    }
}
