package com.phorest.booking.infrastructure.repository.spring;

import com.phorest.booking.infrastructure.repository.document.ServiceEventDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoServiceEventRepository extends MongoRepository<ServiceEventDocument, String> {
    List<ServiceEventDocument> findByServiceIdOrderByVersionAsc(String serviceId);
    List<ServiceEventDocument> findAll(Sort sort);
}
