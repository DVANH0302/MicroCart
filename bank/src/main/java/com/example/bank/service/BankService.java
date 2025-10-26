package com.example.bank.service;

import com.example.bank.dto.request.PaymentRequest;
import com.example.bank.dto.request.RefundRequest;
import com.example.bank.dto.response.AccountBalanceResponse;
import com.example.bank.dto.response.PaymentResponse;
import com.example.bank.dto.response.RefundResponse;
import com.example.bank.dto.response.TransactionResponse;

import java.util.List;

/**
 * Service interface for bank operations
 */
public interface BankService {

    /**
     * Process a payment transaction
     * @param request Payment request details
     * @return Payment response with transaction details
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Process a refund transaction
     * @param request Refund request details
     * @return Refund response with transaction details
     */
    RefundResponse processRefund(RefundRequest request);

    /**
     * Get transaction details by transaction ID
     * @param transactionId Transaction ID
     * @return Transaction details
     */
    TransactionResponse getTransaction(String transactionId);

    /**
     * Get all transactions for a specific order
     * @param orderId Order ID
     * @return List of transactions for the order
     */
    List<TransactionResponse> getTransactionsByOrderId(Integer orderId);

    /**
     * Get account balance
     * @param accountId Account ID
     * @return Account balance details
     */
    AccountBalanceResponse getAccountBalance(String accountId);
}
