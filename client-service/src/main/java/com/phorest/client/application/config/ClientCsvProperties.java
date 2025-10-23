package com.phorest.client.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "client.csv")
public record ClientCsvProperties(List<String> expectedHeaders) {
}
