package com.phorest.booking.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClientEventMongoRepository extends MongoRepository<ClientEventDocument, String> {
    List<ClientEventDocument> findByClientIdOrderByVersionAsc(String clientId);
}
