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
 * Request DTO for creating a new bank account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    /**
     * Unique account identifier (e.g., "CUST_001", "STORE_MAIN")
     */
    @NotBlank(message = "Account ID is required")
    private String accountId;

    /**
     * Account holder's name
     */
    @NotBlank(message = "Holder name is required")
    private String holderName;

    /**
     * Initial balance for the account (must be >= 0)
     */
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Initial balance must be >= 0")
    private BigDecimal initialBalance;
}
