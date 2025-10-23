package com.phorest.client.infrastructure.repository.spring;

import com.phorest.client.infrastructure.repository.document.ClientEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoClientEventRepository extends MongoRepository<ClientEventDocument, String> {
    List<ClientEventDocument> findByClientIdOrderByVersionAsc(String clientId);
}
