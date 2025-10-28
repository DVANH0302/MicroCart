package com.example.bank.service;

import com.example.bank.dto.request.CreateAccountRequest;
import com.example.bank.dto.request.DepositRequest;
import com.example.bank.dto.request.PaymentRequest;
import com.example.bank.dto.request.RefundRequest;
import com.example.bank.dto.request.WithdrawRequest;
import com.example.bank.dto.response.AccountBalanceResponse;
import com.example.bank.dto.response.CreateAccountResponse;
import com.example.bank.dto.response.DepositResponse;
import com.example.bank.dto.response.PaymentResponse;
import com.example.bank.dto.response.RefundResponse;
import com.example.bank.dto.response.TransactionResponse;
import com.example.bank.dto.response.WithdrawResponse;

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

    /**
     * Create a new bank account
     * @param request Account creation request
     * @return Account creation response
     */
    CreateAccountResponse createAccount(CreateAccountRequest request);

    /**
     * Deposit money into an account
     * @param request Deposit request details
     * @return Deposit response with updated balance
     */
    DepositResponse deposit(DepositRequest request);

    /**
     * Withdraw money from an account
     * @param request Withdrawal request details
     * @return Withdrawal response with updated balance
     */
    WithdrawResponse withdraw(WithdrawRequest request);
}
