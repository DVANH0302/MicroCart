package com.example.bank.dto.response;

import com.example.bank.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private String transactionId;
    private String originalTransactionId;
    private TransactionStatus status;
    private String message;
    private BigDecimal amount;
    private Integer orderId;
    private LocalDateTime timestamp;

    /**
     * Create success response
     */
    public static RefundResponse success(String transactionId, String originalTransactionId, BigDecimal amount, Integer orderId) {
        return RefundResponse.builder()
            .transactionId(transactionId)
            .originalTransactionId(originalTransactionId)
            .status(TransactionStatus.SUCCESS)
            .message("Refund processed successfully")
            .amount(amount)
            .orderId(orderId)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create failure response
     */
    public static RefundResponse failure(String originalTransactionId, String message, BigDecimal amount, Integer orderId) {
        return RefundResponse.builder()
            .transactionId(null)
            .originalTransactionId(originalTransactionId)
            .status(TransactionStatus.FAILED)
            .message(message)
            .amount(amount)
            .orderId(orderId)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
