package com.example.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account balance response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

    private String accountId;
    private String holderName;
    private BigDecimal balance;
    private LocalDateTime timestamp;
}
