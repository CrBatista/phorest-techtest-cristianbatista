package com.phorest.booking.domain.exception;

public class BookingCsvParsingException extends RuntimeException {
    public BookingCsvParsingException(String message) {
        super(message);
    }

    public BookingCsvParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
