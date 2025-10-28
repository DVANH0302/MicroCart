package com.example.bank.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for withdrawing money from an account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    /**
     * Account ID to withdraw from
     */
    @NotBlank(message = "Account ID is required")
    private String accountId;

    /**
     * Amount to withdraw (must be > 0)
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Optional description/note for the withdrawal
     */
    private String description;
}
