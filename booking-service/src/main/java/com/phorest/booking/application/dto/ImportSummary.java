package com.phorest.booking.application.dto;

public record ImportSummary(int processed, int created, int updated, int skipped, int deleted) {
}
