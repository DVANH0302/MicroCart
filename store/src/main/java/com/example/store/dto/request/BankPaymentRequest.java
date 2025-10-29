package com.example.store.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BankPaymentRequest {
    private Integer orderId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
}
