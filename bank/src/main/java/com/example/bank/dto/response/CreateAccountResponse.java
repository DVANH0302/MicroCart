package com.example.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountResponse {

    /**
     * Account ID that was created
     */
    private String accountId;

    /**
     * Account holder's name
     */
    private String holderName;

    /**
     * Current balance
     */
    private BigDecimal balance;

    /**
     * Account creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Success message
     */
    private String message;

    /**
     * Create successful response
     */
    public static CreateAccountResponse success(String accountId, String holderName, BigDecimal balance, LocalDateTime createdAt) {
        return CreateAccountResponse.builder()
                .accountId(accountId)
                .holderName(holderName)
                .balance(balance)
                .createdAt(createdAt)
                .message("Account created successfully")
                .build();
    }
}
