package com.example.store.dto.response;

import lombok.Data;

@Data
public class BankRefundResponse {
    private String status;
    private String transactionId;
    private String message;
}