package com.example.store.exception;

public class OrderSagaException extends RuntimeException {
    public OrderSagaException(String message) {
        super(message);
    }
}
