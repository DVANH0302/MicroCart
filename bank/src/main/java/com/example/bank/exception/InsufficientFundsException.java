package com.example.bank.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when account has insufficient funds
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String accountId, BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient funds in account '%s'. Required: %s, Available: %s",
            accountId, required, available));
    }
}
