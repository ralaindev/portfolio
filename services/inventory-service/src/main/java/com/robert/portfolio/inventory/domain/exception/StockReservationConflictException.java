package com.robert.portfolio.inventory.domain.exception;

public class StockReservationConflictException extends RuntimeException {

    public StockReservationConflictException(String message) {
        super(message);
    }
}
