package com.phorest.booking.application.events;

import com.phorest.booking.domain.purchase.PurchaseEvent;

public interface PurchaseEventPublisher {
    void publish(PurchaseEvent event);
}
