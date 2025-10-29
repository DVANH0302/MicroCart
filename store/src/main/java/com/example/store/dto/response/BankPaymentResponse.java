package com.example.store.dto.response;

import lombok.Data;

@Data
public class BankPaymentResponse {
    private String status;
    private String transactionId;
    private String message;
}