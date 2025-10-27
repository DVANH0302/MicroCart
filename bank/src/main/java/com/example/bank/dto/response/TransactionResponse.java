package com.example.bank.dto.response;

import com.example.bank.entity.TransactionStatus;
import com.example.bank.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction response DTO for query endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private Integer orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
