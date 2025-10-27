package com.example.bank.exception;

/**
 * Exception thrown when a duplicate transaction is attempted
 */
public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(String message) {
        super(message);
    }

    public DuplicateTransactionException(Integer orderId, String transactionType) {
        super(String.format("A %s transaction already exists for order %d",
            transactionType, orderId));
    }
}
