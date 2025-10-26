package com.example.bank.exception;

/**
 * Exception thrown when a bank account is not found
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String accountId, boolean isFrom) {
        super(String.format("%s account '%s' not found",
            isFrom ? "Source" : "Destination", accountId));
    }
}
