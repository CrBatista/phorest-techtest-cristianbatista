package com.phorest.loyalty.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceEventMongoRepository extends MongoRepository<ServiceEventDocument, String> {
}
