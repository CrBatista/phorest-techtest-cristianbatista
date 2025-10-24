package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.PurchaseResponse;
import com.phorest.booking.application.dto.PurchaseUpdateRequest;
import com.phorest.booking.application.service.PurchaseCommandService;
import com.phorest.booking.application.service.PurchaseReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final PurchaseReadService readService;
    private final PurchaseCommandService commandService;

    public PurchaseController(PurchaseReadService readService,
                               PurchaseCommandService commandService) {
        this.readService = readService;
        this.commandService = commandService;
    }

    @GetMapping
    public Page<PurchaseResponse> list(Pageable pageable,
                                       @RequestParam(value = "clientId", required = false) String clientId) {
        return readService.getPurchases(pageable, clientId);
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponse> get(@PathVariable("purchaseId") String purchaseId) {
        return readService.getPurchase(purchaseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponse> update(@PathVariable("purchaseId") String purchaseId,
                                                   @Validated @RequestBody PurchaseUpdateRequest request) {
        return commandService.updatePurchase(purchaseId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<Void> delete(@PathVariable("purchaseId") String purchaseId) {
        return commandService.deletePurchase(purchaseId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
