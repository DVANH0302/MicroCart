package com.example.bank.dto.response;

import com.example.bank.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String transactionId;
    private TransactionStatus status;
    private String message;
    private BigDecimal amount;
    private Integer orderId;
    private LocalDateTime timestamp;

    /**
     * Create success response
     */
    public static PaymentResponse success(String transactionId, BigDecimal amount, Integer orderId) {
        return PaymentResponse.builder()
            .transactionId(transactionId)
            .status(TransactionStatus.SUCCESS)
            .message("Payment processed successfully")
            .amount(amount)
            .orderId(orderId)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create failure response
     */
    public static PaymentResponse failure(String transactionId, String message, BigDecimal amount, Integer orderId) {
        return PaymentResponse.builder()
            .transactionId(transactionId)
            .status(TransactionStatus.FAILED)
            .message(message)
            .amount(amount)
            .orderId(orderId)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
