package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.events.PurchaseEventPublisher;
import com.phorest.booking.application.parser.PurchaseCsvParser;
import com.phorest.booking.application.parser.PurchaseCsvParser.PurchaseCsvRecord;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.purchase.PurchaseAggregate;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.domain.purchase.PurchaseEventStore;
import com.phorest.booking.domain.purchase.PurchaseEventType;
import com.phorest.booking.domain.purchase.PurchaseProfile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseImportService {

    private final PurchaseCsvParser parser;
    private final PurchaseEventStore eventStore;
    private final AppointmentQueryService appointmentQueryService;
    private final PurchaseEventPublisher eventPublisher;
    private final Clock clock;

    public PurchaseImportService(PurchaseCsvParser parser,
                                 PurchaseEventStore eventStore,
                                 AppointmentQueryService appointmentQueryService,
                                 PurchaseEventPublisher eventPublisher,
                                 Clock clock) {
        this.parser = parser;
        this.eventStore = eventStore;
        this.appointmentQueryService = appointmentQueryService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public ImportSummary importPurchases(MultipartFile file) {
        List<PurchaseCsvRecord> records = parse(file);
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int deleted = 0;

        for (PurchaseCsvRecord record : records) {
            AppointmentSnapshot appointment = appointmentQueryService.getSnapshot(record.appointmentId()).orElse(null);
            if (appointment == null || appointment.deleted()) {
                skipped++;
                continue;
            }

            PurchaseProfile profile = new PurchaseProfile(
                    record.purchaseId(),
                    record.appointmentId(),
                    appointment.clientId(),
                    record.name(),
                    record.price(),
                    record.loyaltyPoints(),
                    appointment.startTime(),
                    false
            );

            PurchaseAggregate aggregate = PurchaseAggregate.fromEvents(eventStore.loadByPurchaseId(record.purchaseId()));
            var nextType = aggregate.nextEventType(profile);
            if (nextType.isEmpty()) {
                skipped++;
                continue;
            }

            PurchaseEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
            eventStore.append(event);
            eventPublisher.publish(event);

            if (nextType.get() == PurchaseEventType.PURCHASE_CREATED) {
                created++;
            } else if (nextType.get() == PurchaseEventType.PURCHASE_UPDATED) {
                updated++;
            } else {
                deleted++;
            }
        }

        return new ImportSummary(records.size(), created, updated, skipped, deleted);
    }

    private List<PurchaseCsvRecord> parse(MultipartFile file) {
        try {
            return parser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read provided CSV file", e);
        }
    }

    private PurchaseEvent buildEvent(PurchaseProfile profile, PurchaseEventType type, int version) {
        Instant occurredAt = Instant.now(clock);
        return new PurchaseEvent(
                UUID.randomUUID().toString(),
                profile.purchaseId(),
                type,
                profile,
                occurredAt,
                version
        );
    }
}
