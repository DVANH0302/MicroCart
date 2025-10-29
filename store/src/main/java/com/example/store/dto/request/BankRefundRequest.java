package com.example.store.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankRefundRequest {
    private String originalTransactionId;
    private Integer orderId;
}