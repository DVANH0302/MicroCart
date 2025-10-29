package com.example.store.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BankRefundRequest {
    private Integer orderId;
    private String originalTransactionId;
    private BigDecimal amount;
}
