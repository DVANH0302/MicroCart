package com.example.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for deposit operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {

    /**
     * Account ID that received the deposit
     */
    private String accountId;

    /**
     * Amount deposited
     */
    private BigDecimal amount;

    /**
     * New balance after deposit
     */
    private BigDecimal newBalance;

    /**
     * Timestamp of the deposit
     */
    private LocalDateTime timestamp;

    /**
     * Success or failure message
     */
    private String message;

    /**
     * Create successful deposit response
     */
    public static DepositResponse success(String accountId, BigDecimal amount, BigDecimal newBalance) {
        return DepositResponse.builder()
                .accountId(accountId)
                .amount(amount)
                .newBalance(newBalance)
                .timestamp(LocalDateTime.now())
                .message("Deposit successful")
                .build();
    }
}
