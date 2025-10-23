package com.phorest.client.application.dto;

public record ClientImportSummary(
        int processed,
        int created,
        int updated,
        int skipped
) {
}
