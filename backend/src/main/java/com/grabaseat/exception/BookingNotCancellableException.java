package com.grabaseat.exception;

public class BookingNotCancellableException extends RuntimeException {
    public BookingNotCancellableException(String message) {
        super(message);
    }
}
