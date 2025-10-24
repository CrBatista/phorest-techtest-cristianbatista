package com.phorest.booking.infrastructure.repository.spring;

import com.phorest.booking.infrastructure.repository.document.AppointmentEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoAppointmentEventRepository extends MongoRepository<AppointmentEventDocument, String> {
    List<AppointmentEventDocument> findByAppointmentIdOrderByVersionAsc(String appointmentId);
}
