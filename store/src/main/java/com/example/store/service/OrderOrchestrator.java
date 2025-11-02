package com.example.store.service;


import com.example.store.dto.request.OrderRequest;
import com.example.store.dto.response.OrderResponse;

public interface OrderOrchestrator {
    OrderResponse executeOrderCreation(OrderRequest orderRequest);
    void executeRefund(Integer orderId);
}
