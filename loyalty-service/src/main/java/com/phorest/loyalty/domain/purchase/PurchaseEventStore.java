package com.phorest.loyalty.domain.purchase;

import java.util.List;

public interface PurchaseEventStore {
    PurchaseEvent append(PurchaseEvent event);
    List<PurchaseEvent> loadAll();
}
