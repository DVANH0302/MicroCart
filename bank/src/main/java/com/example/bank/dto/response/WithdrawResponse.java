package com.example.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for withdrawal operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {

    /**
     * Account ID from which withdrawal was made
     */
    private String accountId;

    /**
     * Amount withdrawn
     */
    private BigDecimal amount;

    /**
     * New balance after withdrawal
     */
    private BigDecimal newBalance;

    /**
     * Timestamp of the withdrawal
     */
    private LocalDateTime timestamp;

    /**
     * Success or failure message
     */
    private String message;

    /**
     * Create successful withdrawal response
     */
    public static WithdrawResponse success(String accountId, BigDecimal amount, BigDecimal newBalance) {
        return WithdrawResponse.builder()
                .accountId(accountId)
                .amount(amount)
                .newBalance(newBalance)
                .timestamp(LocalDateTime.now())
                .message("Withdrawal successful")
                .build();
    }
}
