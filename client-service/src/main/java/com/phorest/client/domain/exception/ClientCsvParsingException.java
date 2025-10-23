package com.phorest.client.domain.exception;

public class ClientCsvParsingException extends RuntimeException {
    public ClientCsvParsingException(String message) {
        super(message);
    }

    public ClientCsvParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
