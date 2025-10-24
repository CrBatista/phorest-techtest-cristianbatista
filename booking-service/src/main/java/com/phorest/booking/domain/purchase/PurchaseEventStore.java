package com.phorest.booking.domain.purchase;

import java.util.List;

public interface PurchaseEventStore {
    List<PurchaseEvent> loadByPurchaseId(String purchaseId);
    PurchaseEvent append(PurchaseEvent event);
    List<PurchaseEvent> loadAll();
}
