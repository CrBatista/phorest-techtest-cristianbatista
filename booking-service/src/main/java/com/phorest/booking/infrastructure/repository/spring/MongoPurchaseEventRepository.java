package com.phorest.booking.infrastructure.repository.spring;

import com.phorest.booking.infrastructure.repository.document.PurchaseEventDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoPurchaseEventRepository extends MongoRepository<PurchaseEventDocument, String> {
    List<PurchaseEventDocument> findByPurchaseIdOrderByVersionAsc(String purchaseId);
    List<PurchaseEventDocument> findAll(Sort sort);
}
