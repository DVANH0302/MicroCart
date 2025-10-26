package com.example.bank.controller;

import com.example.bank.dto.request.PaymentRequest;
import com.example.bank.dto.request.RefundRequest;
import com.example.bank.dto.response.AccountBalanceResponse;
import com.example.bank.dto.response.PaymentResponse;
import com.example.bank.dto.response.RefundResponse;
import com.example.bank.dto.response.TransactionResponse;
import com.example.bank.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Bank operations
 */
@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
@Slf4j
public class BankController {

    private final BankService bankService;

    /**
     * Process a payment transaction
     * POST /api/bank/payment
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for order {}", request.getOrderId());
        PaymentResponse response = bankService.processPayment(request);

        HttpStatus status = response.getStatus().name().equals("SUCCESS")
            ? HttpStatus.OK
            : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }

    /**
     * Process a refund transaction
     * POST /api/bank/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        log.info("Received refund request for order {}", request.getOrderId());
        RefundResponse response = bankService.processRefund(request);

        HttpStatus status = response.getStatus().name().equals("SUCCESS")
            ? HttpStatus.OK
            : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }

    /**
     * Get transaction details by ID
     * GET /api/bank/transaction/{transactionId}
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        log.info("Received request to fetch transaction: {}", transactionId);
        TransactionResponse response = bankService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions for a specific order
     * GET /api/bank/order/{orderId}/transactions
     */
    @GetMapping("/order/{orderId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getOrderTransactions(@PathVariable Integer orderId) {
        log.info("Received request to fetch transactions for order: {}", orderId);
        List<TransactionResponse> responses = bankService.getTransactionsByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get account balance
     * GET /api/bank/account/{accountId}/balance
     */
    @GetMapping("/account/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(@PathVariable String accountId) {
        log.info("Received request to fetch balance for account: {}", accountId);
        AccountBalanceResponse response = bankService.getAccountBalance(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/bank/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Bank Service is running");
    }
}
